package com.xmon.shortlink.project.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 响应 VO：各维度（浏览器/OS/设备/网络）占比统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkStatsTopRespVO {

    /**
     * 访问量
     */
    private Integer cnt;

    /**
     * 维度值（如 Chrome、macOS、PC、WIFI 等）
     */
    private String value;

    /**
     * 占比（百分比，保留两位小数）
     */
    private Double ratio;
}
