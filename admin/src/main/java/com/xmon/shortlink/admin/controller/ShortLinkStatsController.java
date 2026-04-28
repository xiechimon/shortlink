package com.xmon.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xmon.shortlink.admin.common.convention.result.Result;
import com.xmon.shortlink.admin.remote.ShortLinkRemoteService;
import com.xmon.shortlink.admin.remote.dto.req.LinkAccessLogPageReqDTO;
import com.xmon.shortlink.admin.remote.dto.req.ShortLinkStatsReqDTO;
import com.xmon.shortlink.admin.remote.dto.resp.LinkAccessLogPageRespDTO;
import com.xmon.shortlink.admin.remote.dto.resp.ShortLinkStatsRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接监控统计后管控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    /**
     * TODO 重构为 SpringCloud Feign 调用
     */
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 访问单个短链接监控统计
     */
    @GetMapping("/api/shortlink/admin/v1/stats")
    public Result<ShortLinkStatsRespDTO> oneShortLinkStats(ShortLinkStatsReqDTO requestParam) {
        return shortLinkRemoteService.oneShortLinkStats(requestParam);
    }

    /**
     * 分页查询短链接访问日志
     */
    @GetMapping("/api/shortlink/admin/v1/stats/access-record")
    public Result<IPage<LinkAccessLogPageRespDTO>> pageAccessLog(LinkAccessLogPageReqDTO requestParam) {
        return shortLinkRemoteService.pageAccessLog(requestParam);
    }
}
