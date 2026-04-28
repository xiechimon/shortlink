package com.xmon.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xmon.shortlink.project.dto.req.LinkAccessLogPageReqDTO;
import com.xmon.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.xmon.shortlink.project.dto.resp.LinkAccessLogPageRespDTO;
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

    /**
     * 分页查询短链接访问日志
     *
     * @param requestParam 分页查询参数（短链接、分组、起止日期）
     * @return 访问日志分页结果
     */
    IPage<LinkAccessLogPageRespDTO> pageAccessLog(LinkAccessLogPageReqDTO requestParam);
}
