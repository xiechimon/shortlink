package com.xmon.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.text.StrBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xmon.shortlink.project.common.constant.RedisCacheConstant;
import com.xmon.shortlink.project.common.convention.errorcode.ProjectErrorCodeEnum;
import com.xmon.shortlink.project.common.convention.exception.ClientException;
import com.xmon.shortlink.project.common.convention.exception.ServiceException;
import com.xmon.shortlink.project.common.enums.ValidDateTypeEnum;
import com.xmon.shortlink.project.dao.entity.ShortLinkDO;
import com.xmon.shortlink.project.dao.entity.ShortLinkGotoDO;
import com.xmon.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import com.xmon.shortlink.project.dao.mapper.ShortLinkMapper;
import com.xmon.shortlink.project.dao.mapper.LinkAccessStatsMapper;
import com.xmon.shortlink.project.dao.entity.LinkAccessStatsDO;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import com.xmon.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.xmon.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.xmon.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.xmon.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.xmon.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.xmon.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.xmon.shortlink.project.service.ShortLinkService;
import com.xmon.shortlink.project.tookit.ClientIpUtil;
import com.xmon.shortlink.project.tookit.HashUtil;
import com.xmon.shortlink.project.tookit.ShortLinkCacheUtil;
import com.xmon.shortlink.project.tookit.WebTitleFetcher;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private static final ExecutorService STATS_EXECUTOR = new ThreadPoolExecutor(
            4, 64, 60, TimeUnit.SECONDS, new SynchronousQueue<>(),
            Thread.ofVirtual().name("stats-", 0).factory(),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    private final RBloomFilter<String> shortLinkCachePenetrationBloomFilter;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final WebTitleFetcher webTitleFetcher;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    @Value("${short-link.default-protocol:http}")
    private String defaultProtocol;
    @Value("${short-link.not-found-redirect-url:}")
    private String notFoundRedirectUrl;

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
                .favicon(getFaviconByUrl(requestParam.getOriginUrl()))
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
        warmupShortLinkCache(shortLinkDO);
        return ShortLinkCreateRespDTO.builder()
                .gid(shortLinkDO.getGid())
                .originUrl(shortLinkDO.getOriginUrl())
                .fullShortUrl(buildDisplayShortUrl(shortLinkDO.getFullShortUrl()))
                .build();
    }

    @Override
    @SneakyThrows
    public void restoreUrl(String domain, String shortUri, HttpServletRequest request, HttpServletResponse response) {
        Set<String> candidateFullShortUrls = buildCandidateFullShortUrls(domain, shortUri);
        for (String fullShortUrl : candidateFullShortUrls) {
            if (tryRestoreByFullShortUrl(fullShortUrl, request, response)) {
                return;
            }
        }
        if (notFoundRedirectUrl != null && !notFoundRedirectUrl.isBlank()) {
            response.sendRedirect(notFoundRedirectUrl);
            return;
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private boolean tryRestoreByFullShortUrl(String fullShortUrl, ServletRequest request, ServletResponse response) {
        String cacheKey = RedisCacheConstant.buildGotoShortLinkKey(fullShortUrl);
        String nullCacheKey = RedisCacheConstant.buildGotoIsNullShortLinkKey(fullShortUrl);
        if (stringRedisTemplate.hasKey(nullCacheKey)) {
            return false;
        }
        // 一级：先查 Redis 热缓存，命中则直接返回，避免访问数据库。
        String cachedOriginUrl = stringRedisTemplate.opsForValue().get(cacheKey);
        if (tryRespondFromCache(fullShortUrl, cachedOriginUrl, request, response)) {
            return true;
        }

        if (!shortLinkCachePenetrationBloomFilter.contains(fullShortUrl)) {
            // 布隆判定不存在，写入短期空值缓存，拦截后续同类穿透请求。
            cacheNullShortLink(fullShortUrl);
            return false;
        }

        // 缓存未命中但布隆可能存在，进入互斥重建，避免并发击穿。
        RLock lock = redissonClient.getLock(RedisCacheConstant.buildGotoShortLinkLockKey(fullShortUrl));
        if (!lock.tryLock()) {
            // 未抢到锁，短暂等待后再查一次缓存；若仍未命中则兜底走 DB 路径
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            String retriedOriginUrl = stringRedisTemplate.opsForValue().get(cacheKey);
            if (tryRespondFromCache(fullShortUrl, retriedOriginUrl, request, response)) {
                return true;
            }
            // 缓存仍未就绪，降级：直接查 DB（不加锁，接受少量并发回源）
        }

        try {
            String lockedCachedOriginUrl = stringRedisTemplate.opsForValue().get(cacheKey);
            if (tryRespondFromCache(fullShortUrl, lockedCachedOriginUrl, request, response)) {
                return true;
            }

            LambdaQueryWrapper<ShortLinkGotoDO> gotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl)
                    .last("LIMIT 1");
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(gotoQueryWrapper);
            if (shortLinkGotoDO == null) {
                // 跳转映射不存在，回填空值缓存，减少无效回源。
                cacheNullShortLink(fullShortUrl);
                return false;
            }

            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .last("LIMIT 1");
            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
            if (shortLinkDO == null || ShortLinkCacheUtil.isExpired(shortLinkDO)) {
                // 主表不存在或已过期，写空值缓存，保证后续快速失败。
                cacheNullShortLink(fullShortUrl);
                return false;
            }

            // 回源成功后写回缓存，后续请求走 Redis。
            cacheShortLink(cacheKey, shortLinkDO);
            shortLinkStats(fullShortUrl, shortLinkDO.getGid(), request, response);
            sendRedirect((HttpServletResponse) response, shortLinkDO.getOriginUrl());
            return true;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
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
            evictShortLinkCache(hasShortLinkDO.getFullShortUrl());
            warmupShortLinkCache(buildUpdatedShortLinkDO(hasShortLinkDO, requestParam));
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
        evictShortLinkCache(hasShortLinkDO.getFullShortUrl());
        warmupShortLinkCache(shortLinkDO);
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

    @Override
    public String getTitleByUrl(String url) {
        return webTitleFetcher.fetchTitle(url);
    }

    @Override
    public String getFaviconByUrl(String url) {
        return webTitleFetcher.fetchFavicon(url);
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

    private ShortLinkDO buildUpdatedShortLinkDO(ShortLinkDO hasShortLinkDO, ShortLinkUpdateReqDTO requestParam) {
        return ShortLinkDO.builder()
                .id(hasShortLinkDO.getId())
                .domain(hasShortLinkDO.getDomain())
                .shortUri(hasShortLinkDO.getShortUri())
                .fullShortUrl(hasShortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .clickNum(hasShortLinkDO.getClickNum())
                .gid(hasShortLinkDO.getGid())
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

    private void cacheNullShortLink(String fullShortUrl) {
        String nullCacheKey = RedisCacheConstant.buildGotoIsNullShortLinkKey(fullShortUrl);
        stringRedisTemplate.opsForValue().set(
                nullCacheKey,
                "1",
                RedisCacheConstant.GOTO_LINK_NULL_TTL_SECONDS,
                TimeUnit.SECONDS
        );
    }

    private void cacheShortLink(String cacheKey, ShortLinkDO shortLinkDO) {
        String nullCacheKey = RedisCacheConstant.buildGotoIsNullShortLinkKey(shortLinkDO.getFullShortUrl());
        stringRedisTemplate.delete(nullCacheKey);
        long ttlMillis = ShortLinkCacheUtil.getLinkCacheValidTime(shortLinkDO.getValidDate());
        if (ttlMillis > 0) {
            stringRedisTemplate.opsForValue().set(cacheKey, shortLinkDO.getOriginUrl(), ttlMillis, TimeUnit.MILLISECONDS);
            String gidCacheKey = RedisCacheConstant.buildGotoShortLinkGidKey(shortLinkDO.getFullShortUrl());
            stringRedisTemplate.opsForValue().set(gidCacheKey, shortLinkDO.getGid(), ttlMillis, TimeUnit.MILLISECONDS);
        }
    }

    private void evictShortLinkCache(String fullShortUrl) {
        stringRedisTemplate.delete(RedisCacheConstant.buildGotoShortLinkKey(fullShortUrl));
        stringRedisTemplate.delete(RedisCacheConstant.buildGotoIsNullShortLinkKey(fullShortUrl));
        stringRedisTemplate.delete(RedisCacheConstant.buildGotoShortLinkGidKey(fullShortUrl));
    }

    private void warmupShortLinkCache(ShortLinkDO shortLinkDO) {
        if (shortLinkDO == null || shortLinkDO.getFullShortUrl() == null || ShortLinkCacheUtil.isExpired(shortLinkDO)) {
            return;
        }
        cacheShortLink(RedisCacheConstant.buildGotoShortLinkKey(shortLinkDO.getFullShortUrl()), shortLinkDO);
    }

    @SneakyThrows
    private boolean tryRespondFromCache(String fullShortUrl, String cachedOriginUrl, ServletRequest request, ServletResponse response) {
        if (cachedOriginUrl != null && !cachedOriginUrl.isBlank()) {
            String gidCacheKey = RedisCacheConstant.buildGotoShortLinkGidKey(fullShortUrl);
            String cachedGid = stringRedisTemplate.opsForValue().get(gidCacheKey);
            shortLinkStats(fullShortUrl, cachedGid, request, response);
            sendRedirect((HttpServletResponse) response, cachedOriginUrl);
            return true;
        }
        return false;
    }

    private void shortLinkStats(String fullShortUrl, String gid, ServletRequest request, ServletResponse response) {
        // 提前在主线程中获取 Cookie 相关数据，避免异步线程里操作已提交的 Response
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // UV 判断：读取访客的 UV Cookie
        String uvValue = null;
        Cookie[] cookies = httpRequest.getCookies();
        if (ArrayUtil.isNotEmpty(cookies)) {
            for (Cookie cookie : cookies) {
                if (RedisCacheConstant.STATS_UV_COOKIE_NAME.equals(cookie.getName())) {
                    uvValue = cookie.getValue();
                    break;
                }
            }
        }
        if (uvValue == null) {
            // 新访客：生成 UUID 并写入 Cookie（有效期 1 个月）
            uvValue = UUID.fastUUID().toString();
            Cookie uvCookie = new Cookie(RedisCacheConstant.STATS_UV_COOKIE_NAME, uvValue);
            uvCookie.setMaxAge(RedisCacheConstant.STATS_UV_COOKIE_MAX_AGE);
            uvCookie.setPath("/");
            httpResponse.addCookie(uvCookie);
        }

        // 将 Cookie 值拷贝到 final 变量，供内部匿名类使用
        final String finalUvValue = uvValue;
        final String remoteAddr = ClientIpUtil.getActualIp(httpRequest);

        // 异步执行，避免统计逻辑影响访问性能；异常吞掉日志记录，不抛出，保证访问链路稳定性
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            String uvRedisKey = null;
            Long uvAdded = null;
            String uipRedisKey = null;
            Long uipAdded = null;
            try {
                String finalGid = gid;
                if (finalGid == null) {
                    LambdaQueryWrapper<ShortLinkGotoDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                            .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl)
                            .last("LIMIT 1");
                    ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(queryWrapper);
                    if (shortLinkGotoDO != null) {
                        finalGid = shortLinkGotoDO.getGid();
                    }
                }
                if (finalGid != null) {
                    Date now = new Date();
                    int hour = DateUtil.hour(now, true);
                    int weekday = DateUtil.dayOfWeekEnum(now).getIso8601Value();
                    String today = DateUtil.formatDate(now);

                    uvRedisKey = RedisCacheConstant.buildStatsUvKey(fullShortUrl, today);
                    uvAdded = stringRedisTemplate.opsForSet().add(uvRedisKey, finalUvValue);
                    if (uvAdded != null && uvAdded > 0) {
                        redissonClient.getBucket(uvRedisKey).expire(25, TimeUnit.HOURS);
                    }
                    int uvIncrement = (uvAdded != null && uvAdded > 0) ? 1 : 0;

                    int uipIncrement = 0;
                    if (remoteAddr != null) {
                        uipRedisKey = RedisCacheConstant.buildStatsUipKey(fullShortUrl, today);
                        uipAdded = stringRedisTemplate.opsForSet().add(uipRedisKey, remoteAddr);
                        if (uipAdded != null && uipAdded > 0) {
                            redissonClient.getBucket(uipRedisKey).expire(25, TimeUnit.HOURS);
                        }
                        uipIncrement = (uipAdded != null && uipAdded > 0) ? 1 : 0;
                    }
                    log.info("短链接访问统计: fullShortUrl={}, uvRedisKey={}, uvValue={}, uvAdded={}, uipRedisKey={}, remoteAddr={}, uipAdded={}",
                            fullShortUrl, uvRedisKey, finalUvValue, uvAdded, uipRedisKey, remoteAddr, uipAdded);

                    LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                            .pv(1)
                            .uv(uvIncrement)
                            .uip(uipIncrement)
                            .hour(hour)
                            .weekday(weekday)
                            .fullShortUrl(fullShortUrl)
                            .gid(finalGid)
                            .date(now)
                            .build();
                    linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);
                }
            } catch (Throwable ex) {
                log.error("短链接访问量统计异常 fullShortUrl={}, uvRedisKey={}, uvValue={}, uvAdded={}, uipRedisKey={}, remoteAddr={}, uipAdded={}",
                        fullShortUrl, uvRedisKey, finalUvValue, uvAdded, uipRedisKey, remoteAddr, uipAdded, ex);
            }
        }, STATS_EXECUTOR);
    }

    @SneakyThrows
    private void sendRedirect(HttpServletResponse response, String originUrl) {
        response.sendRedirect(originUrl);
    }

    private Set<String> buildCandidateFullShortUrls(String domain, String shortUri) {
        Set<String> candidates = new LinkedHashSet<>();
        candidates.add(domain + "/" + shortUri);

        String domainWithoutPort = domain.replaceFirst(":\\d+$", "");
        if (!domainWithoutPort.equals(domain)) {
            candidates.add(domainWithoutPort + "/" + shortUri);
        }
        return candidates;
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
