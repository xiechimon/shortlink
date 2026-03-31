package com.xmon.shortlink.admin.controller;

import com.xmon.shortlink.admin.common.convention.result.Result;
import com.xmon.shortlink.admin.common.convention.result.Results;
import com.xmon.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import com.xmon.shortlink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 分组管理控制层
 */
@RestController
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping("/api/shortlink/v1/group")
    public Result<Void> save(@RequestBody ShortLinkGroupSaveReqDTO requestParam) {
        groupService.saveGroup(requestParam.getName());
        return Results.success();

    }

}
