package com.xmon.shortlink.project.controller;

import com.xmon.shortlink.project.common.result.Result;
import com.xmon.shortlink.project.common.result.Results;
import com.xmon.shortlink.project.dto.req.ShortLinkSaveReqDTO;
import com.xmon.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.xmon.shortlink.project.dto.resp.ShortLinkRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    /**
     * 新增短链接
     */
    @PostMapping("/api/shortlink/v1/link")
    public Result<Void> save(@RequestBody ShortLinkSaveReqDTO requestParam) {
        // TODO: 调用 service
        return Results.success();
    }

    /**
     * 查询短链接列表
     */
    @GetMapping("/api/shortlink/v1/link")
    public Result<List<ShortLinkRespDTO>> listByGid(@RequestParam String gid) {
        // TODO: 调用 service
        return Results.success();
    }

    /**
     * 查询短链接详情
     */
    @GetMapping("/api/shortlink/v1/link/{fullShortUrl}")
    public Result<ShortLinkRespDTO> getLink(@PathVariable String fullShortUrl) {
        // TODO: 调用 service
        return Results.success();
    }

    /**
     * 修改短链接
     */
    @PutMapping("/api/shortlink/v1/link")
    public Result<Void> update(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        // TODO: 调用 service
        return Results.success();
    }

    /**
     * 删除短链接
     */
    @DeleteMapping("/api/shortlink/v1/link/{fullShortUrl}")
    public Result<Void> delete(@PathVariable String fullShortUrl) {
        // TODO: 调用 service
        return Results.success();
    }

    /**
     * 启用短链接
     */
    @PutMapping("/api/shortlink/v1/link/enable/{fullShortUrl}")
    public Result<Void> enable(@PathVariable String fullShortUrl) {
        // TODO: 调用 service
        return Results.success();
    }

    /**
     * 禁用短链接
     */
    @PutMapping("/api/shortlink/v1/link/disable/{fullShortUrl}")
    public Result<Void> disable(@PathVariable String fullShortUrl) {
        // TODO: 调用 service
        return Results.success();
    }
}
