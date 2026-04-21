package com.xmon.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xmon.shortlink.admin.common.biz.user.UserContext;
import com.xmon.shortlink.admin.common.convention.result.Result;
import com.xmon.shortlink.admin.common.convention.result.Results;
import com.xmon.shortlink.admin.dao.entity.GroupDO;
import com.xmon.shortlink.admin.dao.mapper.GroupMapper;
import com.xmon.shortlink.admin.remote.ShortLinkRemoteService;
import com.xmon.shortlink.admin.remote.dto.req.RecycleBinPageReqDTO;
import com.xmon.shortlink.admin.remote.dto.req.RecycleBinRecoverReqDTO;
import com.xmon.shortlink.admin.remote.dto.req.RecycleBinRemoveReqDTO;
import com.xmon.shortlink.admin.remote.dto.req.RecycleBinSaveReqDTO;
import com.xmon.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 回收站管理控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {

    private final GroupMapper groupMapper;

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

    /**
     * 分页查询回收站短链接
     * 自动获取当前登录用户旗下所有分组的 gid，无需前端传入
     */
    @GetMapping("/api/shortlink/admin/v1/recycle-bin/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageRecycleBin(RecycleBinPageReqDTO requestParam) {
        // 查询当前登录用户下所有未删除的分组，提取 gidList
        LambdaQueryWrapper<GroupDO> wrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getUsername, UserContext.getUsername());
        List<String> gidList = groupMapper.selectList(wrapper)
                .stream()
                .map(GroupDO::getGid)
                .toList();

        if (gidList == null || gidList.isEmpty()) {
            return Results.success(new Page<>());
        }

        // 将 gidList 填入请求对象后一并透传
        requestParam.setGidList(gidList);
        return Results.success(shortLinkRemoteService.pageRecycleBin(requestParam).getData());
    }

    /**
     * 从回收站恢复短链接
     */
    @PostMapping("/api/shortlink/admin/v1/recycle-bin/recover")
    public Result<Void> recoverRecycleBin(@RequestBody RecycleBinRecoverReqDTO requestParam) {
        shortLinkRemoteService.recoverRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 彻底删除短链接
     */
    @PostMapping("/api/shortlink/admin/v1/recycle-bin/remove")
    public Result<Void> removeRecycleBin(@RequestBody RecycleBinRemoveReqDTO requestParam) {
        shortLinkRemoteService.removeRecycleBin(requestParam);
        return Results.success();
    }
}
