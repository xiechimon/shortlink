package com.xmon.shortlink.admin.remote.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

/**
 * 回收站短链接分页请求参数
 * gidList 由 admin 层根据当前登录用户自动填充，前端无需传入。
 */
@Data
public class RecycleBinPageReqDTO extends Page<Object> {

    /**
     * 分组标识集合，由 admin 控制层自动填充后透传给 project 引擎
     */
    private List<String> gidList;
}
