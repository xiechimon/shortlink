package com.xmon.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xmon.shortlink.project.common.constant.RedisCacheConstant;
import com.xmon.shortlink.project.dao.entity.ShortLinkDO;
import com.xmon.shortlink.project.dao.mapper.ShortLinkMapper;
import com.xmon.shortlink.project.dto.req.RecycleBinPageReqDTO;
import com.xmon.shortlink.project.dto.req.RecycleBinRecoverReqDTO;
import com.xmon.shortlink.project.dto.req.RecycleBinRemoveReqDTO;
import com.xmon.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.xmon.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.xmon.shortlink.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 回收站接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl implements RecycleBinService {

    private final ShortLinkMapper shortLinkMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void saveRecycleBin(RecycleBinSaveReqDTO requestParam) {
        // 构建带有分片键(gid)的更新器，防止分表产生全表扫描
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0);
        
        ShortLinkDO updateDO = ShortLinkDO.builder()
                .enableStatus(1)
                .build();
        shortLinkMapper.update(updateDO, updateWrapper);

        // 删除已有缓存，拦截后续跳转，使其触发读库并遇到 enableStatus=1
        stringRedisTemplate.delete(RedisCacheConstant.buildGotoShortLinkKey(requestParam.getFullShortUrl()));
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageRecycleBin(RecycleBinPageReqDTO requestParam) {
        if (CollUtil.isEmpty(requestParam.getGidList())) {
            return new Page<>();
        }

        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .in(ShortLinkDO::getGid, requestParam.getGidList())
                .eq(ShortLinkDO::getEnableStatus, 1)
                .eq(ShortLinkDO::getDelFlag, 0)
                .orderByDesc(ShortLinkDO::getUpdateTime);

        IPage<ShortLinkDO> resultPage = shortLinkMapper.selectPage(requestParam, queryWrapper);
        return resultPage.convert(each -> {
            ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            return result;
        });
    }

    @Override
    public void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam) {
        // 构建更新条件：必须是当前分组下的已被移动到回收站的用户
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 1)
                .eq(ShortLinkDO::getDelFlag, 0);

        // 更新状态：恢复 enableStatus 为 0
        ShortLinkDO updateDO = ShortLinkDO.builder()
                .enableStatus(0)
                .build();
        shortLinkMapper.update(updateDO, updateWrapper);

        // 删除当初由于 enableStatus=1 进而导致的 Redis 空值缓存，以便下一次正常路由访问时触发从库中重新加载数据
        stringRedisTemplate.delete(RedisCacheConstant.buildGotoIsNullShortLinkKey(requestParam.getFullShortUrl()));
    }

    @Override
    public void removeRecycleBin(RecycleBinRemoveReqDTO requestParam) {
        // 构建删除条件：必须是当前分组下的已被移动到回收站的用户
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 1)
                .eq(ShortLinkDO::getDelFlag, 0);

        // 执行物理删除
        shortLinkMapper.delete(queryWrapper);

        // 删除遗留的可能空值缓存（保险措施）
        stringRedisTemplate.delete(RedisCacheConstant.buildGotoIsNullShortLinkKey(requestParam.getFullShortUrl()));
    }
}
