package com.xmon.shortlink.project.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 单个短链接监控统计响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkStatsRespDTO {

    // =================== 汇总 ===================

    /**
     * 总访问量
     */
    private Integer pv;

    /**
     * 独立访客总数
     */
    private Integer uv;

    /**
     * 独立 IP 总数
     */
    private Integer uip;

    // =================== 时间维度 ===================

    /**
     * 每日 PV/UV/UIP 列表（按日期升序）
     */
    private List<ShortLinkStatsDailyRespVO> daily;

    /**
     * 24 小时访问分布（索引 0~23 对应 0~23 点，值为该小时总 PV）
     */
    private List<Integer> hourStats;

    /**
     * 星期访问分布（索引 0~6 对应星期一~星期日，值为该星期总 PV）
     */
    private List<Integer> weekdayStats;

    // =================== 访客属性维度 ===================

    /**
     * 浏览器分布
     */
    private List<ShortLinkStatsTopRespVO> browserStats;

    /**
     * 操作系统分布
     */
    private List<ShortLinkStatsTopRespVO> osStats;

    /**
     * 访问设备分布
     */
    private List<ShortLinkStatsTopRespVO> deviceStats;

    /**
     * 访问网络分布
     */
    private List<ShortLinkStatsTopRespVO> networkStats;

    /**
     * 高频访问 IP 列表
     */
    private List<ShortLinkStatsTopRespVO> topIpStats;

    /**
     * 地区分布（省级）
     */
    private List<ShortLinkStatsLocaleCNRespVO> localeStats;

    /**
     * 新老访客分布
     */
    private List<ShortLinkStatsUvRespVO> uvTypeStats;
}
