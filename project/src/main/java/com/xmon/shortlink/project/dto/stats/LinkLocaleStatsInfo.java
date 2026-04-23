package com.xmon.shortlink.project.dto.stats;

import lombok.Builder;
import lombok.Value;

/**
 * 短链接地区统计信息。
 * 该对象用于承载 IP 解析后的地理位置结果，供访问统计落库使用。
 */
@Value
@Builder
public class LinkLocaleStatsInfo {

    /**
     * 省份名称
     */
    String province;

    /**
     * 城市名称
     */
    String city;

    /**
     * 行政区划编码
     */
    String adcode;

    /**
     * 国家名称
     */
    String country;
}
