package com.xmon.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xmon.shortlink.admin.common.convention.result.Result;
import com.xmon.shortlink.admin.common.convention.result.Results;
import com.xmon.shortlink.admin.remote.ShortLinkRemoteService;
import com.xmon.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.xmon.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.xmon.shortlink.admin.remote.dto.req.ShortLinkUpdateReqDTO;
import com.xmon.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.xmon.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接后管控制层
 */
@RestController
public class ShortLinkController {

    /**
     * TODO 重构为 SpringCloud Feign 调用
     */
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };


    /**
     * 新增短链接
     */
    @PostMapping("/api/shortlink/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        requestParam.setDomain(normalizeUrlPrefix(requestParam.getDomain()));
        return shortLinkRemoteService.createShortLink(requestParam);
    }

    /**
     * 修改短链接
     */
    @PutMapping("/api/shortlink/admin/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        requestParam.setFullShortUrl(normalizeUrlPrefix(requestParam.getFullShortUrl()));
        return shortLinkRemoteService.updateShortLink(requestParam);
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/shortlink/admin/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return Results.success(shortLinkRemoteService.pageShortLink(requestParam).getData());
    }

    private String normalizeUrlPrefix(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceFirst("^https?://", "");
    }

}
