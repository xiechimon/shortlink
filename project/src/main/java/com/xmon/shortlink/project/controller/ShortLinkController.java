package com.xmon.shortlink.project.controller;

import com.xmon.shortlink.project.common.result.Result;
import com.xmon.shortlink.project.common.result.Results;
import com.xmon.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.xmon.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.xmon.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * 新增短链接
     */
    @PostMapping("/api/shortlink/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return Results.success(shortLinkService.createShortLink(requestParam));
    }
}
