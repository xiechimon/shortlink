package com.xmon.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 短链接分组排序请求参数
 */
@Data
public class ShortLinkGroupSortReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 排序序号
     */
    private Integer sortOrder;
}
