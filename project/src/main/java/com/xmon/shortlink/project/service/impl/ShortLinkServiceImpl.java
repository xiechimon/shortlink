package com.xmon.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmon.shortlink.project.common.convention.exception.ClientException;
import com.xmon.shortlink.project.common.convention.exception.ServiceException;
import com.xmon.shortlink.project.common.convention.errorcode.ProjectErrorCodeEnum;
import com.xmon.shortlink.project.common.enums.ValidDateTypeEnum;
import com.xmon.shortlink.project.dao.entity.ShortLinkDO;
import com.xmon.shortlink.project.dao.entity.ShortLinkGotoDO;
import com.xmon.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import com.xmon.shortlink.project.dao.mapper.ShortLinkMapper;
import com.xmon.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.xmon.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.xmon.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.xmon.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.xmon.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.xmon.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.xmon.shortlink.project.service.ShortLinkService;
import com.xmon.shortlink.project.tookit.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortLinkCachePenetrationBloomFilter;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    @Value("${short-link.default-protocol:http}")
    private String defaultProtocol;

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        String domain = normalizeDomain(requestParam.getDomain());
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = StrBuilder
                .create(domain)
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(domain)
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .fullShortUrl(fullShortUrl)
                .shortUri(shortLinkSuffix)
                .enableStatus(0)
                .build();
        try {
            baseMapper.insert(shortLinkDO);
            shortLinkGotoMapper.insert(ShortLinkGotoDO.builder()
                    .gid(shortLinkDO.getGid())
                    .fullShortUrl(shortLinkDO.getFullShortUrl())
                    .build());
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ProjectErrorCodeEnum.LINK_EXIST);
        }

        shortLinkCachePenetrationBloomFilter.add(shortLinkDO.getFullShortUrl());
        return ShortLinkCreateRespDTO.builder()
                .gid(shortLinkDO.getGid())
                .originUrl(shortLinkDO.getOriginUrl())
                .fullShortUrl(buildDisplayShortUrl(shortLinkDO.getFullShortUrl()))
                .build();
    }

    @Override
    @SneakyThrows
    public void restoreUrl(String domain, String shortUri, HttpServletRequest request, HttpServletResponse response) {
        String fullShortUrl = domain + "/" + shortUri;
        LambdaQueryWrapper<ShortLinkGotoDO> gotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl)
                .last("LIMIT 1");
        ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(gotoQueryWrapper);
        if (shortLinkGotoDO == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0)
                .last("LIMIT 1");
        ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
        if (shortLinkDO == null || isExpired(shortLinkDO)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.sendRedirect(shortLinkDO.getOriginUrl());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        // 1. 前置校验：先按旧分组查询原短链接，避免无效更新请求继续往后执行
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getOriginGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
        if (hasShortLinkDO == null) {
            throw new ClientException(ProjectErrorCodeEnum.LINK_NOT_FOUND);
        }

        // 2. 构建更新后的完整对象：旧数据保留，不可变字段沿用，新输入覆盖可编辑字段
        // 3. 同组直接更新；跨组时新增新记录并逻辑删除旧记录，避免直接修改分片键/复合索引前缀
        if (Objects.equals(hasShortLinkDO.getGid(), requestParam.getGid())) {
            ShortLinkDO shortLinkDO = buildSameGroupUpdateDO(requestParam);
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getId, hasShortLinkDO.getId())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .set(Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERMANENT.getType()), ShortLinkDO::getValidDate, null);
            baseMapper.update(shortLinkDO, updateWrapper);
            return;
        }

        ShortLinkDO shortLinkDO = buildMoveGroupShortLinkDO(hasShortLinkDO, requestParam);
        try {
            baseMapper.insert(shortLinkDO);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ProjectErrorCodeEnum.LINK_EXIST);
        }

        ShortLinkGotoDO shortLinkGotoDO = new ShortLinkGotoDO();
        shortLinkGotoDO.setGid(requestParam.getGid());
        LambdaUpdateWrapper<ShortLinkGotoDO> gotoUpdateWrapper = Wrappers.lambdaUpdate(ShortLinkGotoDO.class)
                .eq(ShortLinkGotoDO::getFullShortUrl, hasShortLinkDO.getFullShortUrl());
        shortLinkGotoMapper.update(shortLinkGotoDO, gotoUpdateWrapper);

        ShortLinkDO deleteShortLinkDO = new ShortLinkDO();
        deleteShortLinkDO.setDelFlag(1);
        LambdaUpdateWrapper<ShortLinkDO> deleteWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getId, hasShortLinkDO.getId())
                .eq(ShortLinkDO::getDelFlag, 0);
        baseMapper.update(deleteShortLinkDO, deleteWrapper);
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0)
                .orderByDesc(ShortLinkDO::getCreateTime);

        IPage<ShortLinkDO> resultPage = baseMapper.selectPage(requestParam, queryWrapper);
        return resultPage.convert(each -> {
            ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            result.setFullShortUrl(buildDisplayShortUrl(each.getFullShortUrl()));
            return result;
        });
    }

    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<ShortLinkDO> queryWrapper = Wrappers.query();
        queryWrapper.select("gid", "count(*) as shortLinkCount")
                .in("gid", requestParam)
                .eq("enable_status", 0)
                .eq("del_flag", 0)
                .groupBy("gid");
        List<Map<String, Object>> shortLinkDOList = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(shortLinkDOList, ShortLinkGroupCountQueryRespDTO.class);
    }

    private ShortLinkDO buildSameGroupUpdateDO(ShortLinkUpdateReqDTO requestParam) {
        return ShortLinkDO.builder()
                .originUrl(requestParam.getOriginUrl())
                .validDateType(requestParam.getValidDateType())
                .validDate(Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERMANENT.getType()) ? null : requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .build();
    }

    private ShortLinkDO buildMoveGroupShortLinkDO(ShortLinkDO hasShortLinkDO, ShortLinkUpdateReqDTO requestParam) {
        return ShortLinkDO.builder()
                .domain(hasShortLinkDO.getDomain())
                .shortUri(hasShortLinkDO.getShortUri())
                .fullShortUrl(hasShortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .clickNum(hasShortLinkDO.getClickNum())
                .gid(requestParam.getGid())
                .favicon(hasShortLinkDO.getFavicon())
                .enableStatus(hasShortLinkDO.getEnableStatus())
                .createdType(hasShortLinkDO.getCreatedType())
                .validDateType(requestParam.getValidDateType())
                .validDate(Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERMANENT.getType()) ? null : requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .build();
    }

    private String normalizeDomain(String domain) {
        if (domain == null) {
            return null;
        }
        return domain.replaceFirst("^https?://", "");
    }

    private String buildDisplayShortUrl(String fullShortUrl) {
        return defaultProtocol + "://" + fullShortUrl;
    }

    private boolean isExpired(ShortLinkDO shortLinkDO) {
        return Objects.equals(shortLinkDO.getValidDateType(), ValidDateTypeEnum.CUSTOM.getType())
                && shortLinkDO.getValidDate() != null
                && shortLinkDO.getValidDate().before(new java.util.Date());
    }

    private String generateSuffix(ShortLinkCreateReqDTO requestParam) {
        int customGenerateCount = 0;
        String shortUri;
        String domain = normalizeDomain(requestParam.getDomain());
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException(ProjectErrorCodeEnum.LINK_GENERATE_TOO_MANY);
            }
            String originUrl = requestParam.getOriginUrl();
            originUrl += System.currentTimeMillis();
            shortUri = HashUtil.hashToBase62(originUrl);
            String fullShortUrl = domain + "/" + shortUri;
            // 布隆过滤器中不存在，说明该短链接没有被使用过，可以直接使用；如果存在，则说明可能被使用过，需要继续生成新的短链接
            if (!shortLinkCachePenetrationBloomFilter.contains(fullShortUrl)) {
                // 此时短链接一定未被使用
                break;
            }
            customGenerateCount++;
        }

        return shortUri;
    }
}
