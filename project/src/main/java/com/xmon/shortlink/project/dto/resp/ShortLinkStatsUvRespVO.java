package com.xmon.shortlink.project.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 响应 VO：新老访客占比统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkStatsUvRespVO {

    /**
     * 访客类型（newUser / oldUser）
     */
    private String uvType;

    /**
     * 访客数
     */
    private Integer cnt;

    /**
     * 占比（百分比，保留两位小数）
     */
    private Double ratio;
}
