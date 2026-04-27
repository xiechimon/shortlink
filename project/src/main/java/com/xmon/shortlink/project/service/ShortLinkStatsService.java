package com.xmon.shortlink.project.service;

import com.xmon.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.xmon.shortlink.project.dto.resp.ShortLinkStatsRespDTO;

/**
 * 短链接监控统计接口层
 */
public interface ShortLinkStatsService {

    /**
     * 获取单个短链接监控统计
     *
     * @param requestParam 查询参数（短链接、分组、起止日期）
     * @return 监控统计聚合结果
     */
    ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO requestParam);
}
