package com.xmon.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xmon.shortlink.project.dao.entity.LinkAccessLogsDO;
import com.xmon.shortlink.project.dao.entity.LinkAccessStatsDO;
import com.xmon.shortlink.project.dao.entity.LinkBrowserStatsDO;
import com.xmon.shortlink.project.dao.entity.LinkDeviceStatsDO;
import com.xmon.shortlink.project.dao.entity.LinkLocaleStatsDO;
import com.xmon.shortlink.project.dao.entity.LinkNetworkStatsDO;
import com.xmon.shortlink.project.dao.entity.LinkOsStatsDO;
import com.xmon.shortlink.project.dao.mapper.LinkAccessLogsMapper;
import com.xmon.shortlink.project.dao.mapper.LinkAccessStatsMapper;
import com.xmon.shortlink.project.dao.mapper.LinkBrowserStatsMapper;
import com.xmon.shortlink.project.dao.mapper.LinkDeviceStatsMapper;
import com.xmon.shortlink.project.dao.mapper.LinkLocaleStatsMapper;
import com.xmon.shortlink.project.dao.mapper.LinkNetworkStatsMapper;
import com.xmon.shortlink.project.dao.mapper.LinkOsStatsMapper;
import com.xmon.shortlink.project.dto.req.LinkAccessLogPageReqDTO;
import com.xmon.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.xmon.shortlink.project.dto.resp.LinkAccessLogPageRespDTO;
import com.xmon.shortlink.project.dto.resp.ShortLinkStatsDailyRespVO;
import com.xmon.shortlink.project.dto.resp.ShortLinkStatsLocaleCNRespVO;
import com.xmon.shortlink.project.dto.resp.ShortLinkStatsRespDTO;
import com.xmon.shortlink.project.dto.resp.ShortLinkStatsTopRespVO;
import com.xmon.shortlink.project.dto.resp.ShortLinkStatsUvRespVO;
import com.xmon.shortlink.project.service.ShortLinkStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 短链接监控统计实现层
 */
@Service
@RequiredArgsConstructor
public class ShortLinkStatsServiceImpl implements ShortLinkStatsService {

    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;

    @Override
    public ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO requestParam) {
        String fullShortUrl = requestParam.getFullShortUrl();
        String gid = requestParam.getGid();
        String startDate = requestParam.getStartDate();
        String endDate = requestParam.getEndDate();

        // ─── 1. 基础访问统计（每行 = 某天某小时的 PV/UV/UIP）────────────────────
        List<LinkAccessStatsDO> accessStatsList =
                linkAccessStatsMapper.listStatsByLink(fullShortUrl, gid, startDate, endDate);

        // 1a. 每日汇总：按 date 聚合 PV/UV/UIP
        Map<String, ShortLinkStatsDailyRespVO> dailyMap = IntStream
                .rangeClosed(0, (int) DateUtil.betweenDay(DateUtil.parseDate(startDate), DateUtil.parseDate(endDate), true))
                .mapToObj(each -> DateUtil.formatDate(DateUtil.offsetDay(DateUtil.parseDate(startDate), each)))
                .collect(Collectors.toMap(
                        each -> each,
                        each -> ShortLinkStatsDailyRespVO.builder().date(each).pv(0).uv(0).uip(0).build(),
                        (oldVal, newVal) -> oldVal,
                        LinkedHashMap::new
                ));
        for (LinkAccessStatsDO row : accessStatsList) {
            String dateStr = DateUtil.formatDate(row.getDate());
            ShortLinkStatsDailyRespVO daily = dailyMap.computeIfAbsent(dateStr,
                    d -> ShortLinkStatsDailyRespVO.builder().date(d).pv(0).uv(0).uip(0).build());
            daily.setPv(daily.getPv() + row.getPv());
            daily.setUv(daily.getUv() + row.getUv());
            daily.setUip(daily.getUip() + row.getUip());
        }
        List<ShortLinkStatsDailyRespVO> daily = new ArrayList<>(dailyMap.values());

        // 1b. 总计
        int totalPv = daily.stream().mapToInt(ShortLinkStatsDailyRespVO::getPv).sum();
        int totalUv = daily.stream().mapToInt(ShortLinkStatsDailyRespVO::getUv).sum();
        int totalUip = daily.stream().mapToInt(ShortLinkStatsDailyRespVO::getUip).sum();

        // 1c. 24 小时分布（index = hour，value = 累计 PV）
        Integer[] hourArray = new Integer[24];
        Arrays.fill(hourArray, 0);
        for (LinkAccessStatsDO row : accessStatsList) {
            if (row.getHour() != null) {
                hourArray[row.getHour()] += row.getPv();
            }
        }
        List<Integer> hourStats = Arrays.asList(hourArray);

        // 1d. 星期分布（ISO: 1=Mon … 7=Sun，转换为 index 0~6）
        Integer[] weekdayArray = new Integer[7];
        Arrays.fill(weekdayArray, 0);
        for (LinkAccessStatsDO row : accessStatsList) {
            if (row.getWeekday() != null) {
                int idx = row.getWeekday() - 1; // ISO 1~7 → index 0~6
                weekdayArray[idx] += row.getPv();
            }
        }
        List<Integer> weekdayStats = Arrays.asList(weekdayArray);

        // ─── 2. 浏览器分布 ───────────────────────────────────────────────────────
        List<LinkBrowserStatsDO> browserRaw =
                linkBrowserStatsMapper.listStatsByLink(fullShortUrl, gid, startDate, endDate);
        int browserTotal = browserRaw.stream().mapToInt(LinkBrowserStatsDO::getCnt).sum();
        List<ShortLinkStatsTopRespVO> browserStats = browserRaw.stream()
                .map(b -> ShortLinkStatsTopRespVO.builder()
                        .value(b.getBrowser())
                        .cnt(b.getCnt())
                        .ratio(browserTotal == 0 ? 0.0
                                : Math.round(b.getCnt() * 1.0 / browserTotal * 100 * 100.0) / 100.0)
                        .build())
                .collect(Collectors.toList());

        // ─── 3. 操作系统分布 ─────────────────────────────────────────────────────
        List<LinkOsStatsDO> osRaw =
                linkOsStatsMapper.listStatsByLink(fullShortUrl, gid, startDate, endDate);
        int osTotal = osRaw.stream().mapToInt(LinkOsStatsDO::getCnt).sum();
        List<ShortLinkStatsTopRespVO> osStats = osRaw.stream()
                .map(o -> ShortLinkStatsTopRespVO.builder()
                        .value(o.getOs())
                        .cnt(o.getCnt())
                        .ratio(osTotal == 0 ? 0.0
                                : Math.round(o.getCnt() * 1.0 / osTotal * 100 * 100.0) / 100.0)
                        .build())
                .collect(Collectors.toList());

        // ─── 4. 设备分布 ─────────────────────────────────────────────────────────
        List<LinkDeviceStatsDO> deviceRaw =
                linkDeviceStatsMapper.listStatsByLink(fullShortUrl, gid, startDate, endDate);
        int deviceTotal = deviceRaw.stream().mapToInt(LinkDeviceStatsDO::getCnt).sum();
        List<ShortLinkStatsTopRespVO> deviceStats = deviceRaw.stream()
                .map(d -> ShortLinkStatsTopRespVO.builder()
                        .value(d.getDevice())
                        .cnt(d.getCnt())
                        .ratio(deviceTotal == 0 ? 0.0
                                : Math.round(d.getCnt() * 1.0 / deviceTotal * 100 * 100.0) / 100.0)
                        .build())
                .collect(Collectors.toList());

        // ─── 5. 网络分布 ─────────────────────────────────────────────────────────
        List<LinkNetworkStatsDO> networkRaw =
                linkNetworkStatsMapper.listStatsByLink(fullShortUrl, gid, startDate, endDate);
        int networkTotal = networkRaw.stream().mapToInt(LinkNetworkStatsDO::getCnt).sum();
        List<ShortLinkStatsTopRespVO> networkStats = networkRaw.stream()
                .map(n -> ShortLinkStatsTopRespVO.builder()
                        .value(n.getNetwork())
                        .cnt(n.getCnt())
                        .ratio(networkTotal == 0 ? 0.0
                                : Math.round(n.getCnt() * 1.0 / networkTotal * 100 * 100.0) / 100.0)
                        .build())
                .collect(Collectors.toList());

        // ─── 6. 地区分布（省级） ─────────────────────────────────────────────────
        List<LinkLocaleStatsDO> localeRaw =
                linkLocaleStatsMapper.listStatsByLink(fullShortUrl, gid, startDate, endDate);
        int localeTotal = localeRaw.stream().mapToInt(LinkLocaleStatsDO::getCnt).sum();
        List<ShortLinkStatsLocaleCNRespVO> localeStats = localeRaw.stream()
                .map(l -> ShortLinkStatsLocaleCNRespVO.builder()
                        .province(l.getProvince())
                        .cnt(l.getCnt())
                        .ratio(localeTotal == 0 ? 0.0
                                : Math.round(l.getCnt() * 1.0 / localeTotal * 100 * 100.0) / 100.0)
                        .build())
                .collect(Collectors.toList());

        // ─── 7. 新老访客 ─────────────────────────────────────────────────────────
        // 日期范围内所有独立访客
        List<String> uvUsers = linkAccessLogsMapper.listUvUsersByLink(fullShortUrl, gid, startDate, endDate);
        // startDate 之前就访问过该短链接的用户（老访客）
        Set<String> oldUsers = new HashSet<>(
                linkAccessLogsMapper.listOldUsersByLink(fullShortUrl, gid, startDate));

        int oldCnt = (int) uvUsers.stream().filter(oldUsers::contains).count();
        int newCnt = uvUsers.size() - oldCnt;
        int uvTypeTotal = uvUsers.size();

        List<ShortLinkStatsUvRespVO> uvTypeStats = new ArrayList<>();
        uvTypeStats.add(ShortLinkStatsUvRespVO.builder()
                .uvType("newUser")
                .cnt(newCnt)
                .ratio(uvTypeTotal == 0 ? 0.0
                        : Math.round(newCnt * 1.0 / uvTypeTotal * 100 * 100.0) / 100.0)
                .build());
        uvTypeStats.add(ShortLinkStatsUvRespVO.builder()
                .uvType("oldUser")
                .cnt(oldCnt)
                .ratio(uvTypeTotal == 0 ? 0.0
                        : Math.round(oldCnt * 1.0 / uvTypeTotal * 100 * 100.0) / 100.0)
                .build());

        // ─── 8. 高频访问 IP ─────────────────────────────────────────────────────
        List<ShortLinkStatsTopRespVO> topIpStats =
                linkAccessLogsMapper.listTopIpStatsByLink(fullShortUrl, gid, startDate, endDate);
        topIpStats.forEach(each -> each.setRatio(totalPv == 0 ? 0.0
                : Math.round(each.getCnt() * 1.0 / totalPv * 100 * 100.0) / 100.0));

        // ─── 组装返回 ─────────────────────────────────────────────────────────────
        return ShortLinkStatsRespDTO.builder()
                .pv(totalPv)
                .uv(totalUv)
                .uip(totalUip)
                .daily(daily)
                .hourStats(hourStats)
                .weekdayStats(weekdayStats)
                .browserStats(browserStats)
                .osStats(osStats)
                .deviceStats(deviceStats)
                .networkStats(networkStats)
                .topIpStats(topIpStats)
                .localeStats(localeStats)
                .uvTypeStats(uvTypeStats)
                .build();
    }

    @Override
    public IPage<LinkAccessLogPageRespDTO> pageAccessLog(LinkAccessLogPageReqDTO requestParam) {
        String startDate = requestParam.getStartDate();
        String endDate = requestParam.getEndDate();
        LambdaQueryWrapper<LinkAccessLogsDO> queryWrapper = Wrappers.lambdaQuery(LinkAccessLogsDO.class)
                .eq(LinkAccessLogsDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(LinkAccessLogsDO::getGid, requestParam.getGid())
                .eq(LinkAccessLogsDO::getDelFlag, 0)
                .apply(startDate != null && !startDate.isBlank(), "DATE(create_time) >= {0}", startDate)
                .apply(endDate != null && !endDate.isBlank(), "DATE(create_time) <= {0}", endDate)
                .orderByDesc(LinkAccessLogsDO::getCreateTime);

        IPage<LinkAccessLogsDO> resultPage = linkAccessLogsMapper.selectPage(requestParam, queryWrapper);
        Set<String> oldUsers = startDate != null && !startDate.isBlank()
                ? new HashSet<>(linkAccessLogsMapper.listOldUsersByLink(
                requestParam.getFullShortUrl(), requestParam.getGid(), startDate))
                : new HashSet<>();
        return resultPage.convert(each -> {
            LinkAccessLogPageRespDTO result = BeanUtil.toBean(each, LinkAccessLogPageRespDTO.class);
            result.setUvType(oldUsers.contains(each.getUser()) ? "oldUser" : "newUser");
            return result;
        });
    }
}
