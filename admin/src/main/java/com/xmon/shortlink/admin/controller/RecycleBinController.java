package com.xmon.shortlink.admin.controller;

import com.xmon.shortlink.admin.common.convention.result.Result;
import com.xmon.shortlink.admin.common.convention.result.Results;
import com.xmon.shortlink.admin.remote.ShortLinkRemoteService;
import com.xmon.shortlink.admin.remote.dto.req.RecycleBinSaveReqDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回收站管理控制层
 */
@RestController
public class RecycleBinController {

    /**
     * 后续需重构为 SpringCloud Feign 调用
     */
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 将短链接移至回收站
     */
    @PostMapping("/api/shortlink/admin/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) {
        shortLinkRemoteService.saveRecycleBin(requestParam);
        return Results.success();
    }
}
