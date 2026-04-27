package com.xmon.shortlink.admin.remote.dto.req;

import lombok.Data;

/**
 * 单个短链接监控统计请求参数（Admin 远程调用）
 */
@Data
public class ShortLinkStatsReqDTO {

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 起始日期（格式：yyyy-MM-dd）
     */
    private String startDate;

    /**
     * 结束日期（格式：yyyy-MM-dd）
     */
    private String endDate;
}
