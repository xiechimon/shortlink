package com.xmon.shortlink.project.dto.req;

import lombok.Data;

import java.util.Date;

/**
 * 短链接创建参数
 */
@Data
public class ShortLinkSaveReqDTO {

    /**
     * 域名
     */
    private String domain;

    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 有效期类型 0: 永久有效 1: 用户自定义
     */
    private Integer validDateType;

    /**
     * 有效期
     */
    private Date validDate;

    /**
     * 描述
     */
    private String describe;
}
