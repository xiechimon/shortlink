package com.xmon.shortlink.project.controller;

import com.xmon.shortlink.project.common.result.Result;
import com.xmon.shortlink.project.common.result.Results;
import com.xmon.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.xmon.shortlink.project.dto.resp.ShortLinkStatsRespDTO;
import com.xmon.shortlink.project.service.ShortLinkStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接监控统计控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    private final ShortLinkStatsService shortLinkStatsService;

    /**
     * 访问单个短链接监控统计
     */
    @GetMapping("/api/shortlink/v1/stats")
    public Result<ShortLinkStatsRespDTO> oneShortLinkStats(ShortLinkStatsReqDTO requestParam) {
        return Results.success(shortLinkStatsService.oneShortLinkStats(requestParam));
    }
}
