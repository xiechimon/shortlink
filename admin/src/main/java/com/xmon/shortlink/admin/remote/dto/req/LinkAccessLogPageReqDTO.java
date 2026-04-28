package com.xmon.shortlink.admin.remote.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xmon.shortlink.project.dao.entity.LinkAccessLogsDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 短链接访问日志分页请求参数（Admin 远程调用）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LinkAccessLogPageReqDTO extends Page<LinkAccessLogsDO> {

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 起始日期（格式：yyyy-MM-dd）
     */
    private String startDate;

    /**
     * 结束日期（格式：yyyy-MM-dd）
     */
    private String endDate;
}
