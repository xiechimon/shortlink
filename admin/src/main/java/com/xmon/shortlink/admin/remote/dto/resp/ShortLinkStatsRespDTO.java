package com.xmon.shortlink.admin.remote.dto.resp;

import lombok.Data;

import java.util.List;

/**
 * 单个短链接监控统计响应（Admin 远程调用）
 */
@Data
public class ShortLinkStatsRespDTO {

    // =================== 汇总 ===================
    private Integer pv;
    private Integer uv;
    private Integer uip;

    // =================== 时间维度 ===================
    private List<DailyVO> daily;
    private List<Integer> hourStats;
    private List<Integer> weekdayStats;

    // =================== 访客属性维度 ===================
    private List<TopVO> browserStats;
    private List<TopVO> osStats;
    private List<TopVO> deviceStats;
    private List<TopVO> networkStats;
    private List<TopVO> topIpStats;
    private List<LocaleVO> localeStats;
    private List<UvTypeVO> uvTypeStats;

    @Data
    public static class DailyVO {
        private String date;
        private Integer pv;
        private Integer uv;
        private Integer uip;
    }

    @Data
    public static class TopVO {
        private Integer cnt;
        private String value;
        private Double ratio;
    }

    @Data
    public static class LocaleVO {
        private Integer cnt;
        private String province;
        private Double ratio;
    }

    @Data
    public static class UvTypeVO {
        private String uvType;
        private Integer cnt;
        private Double ratio;
    }
}
