package com.xmon.shortlink.project.dto.req;

import lombok.Data;

import java.util.Date;

/**
 * 短链接修改参数
 */
@Data
public class ShortLinkUpdateReqDTO {

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 描述
     */
    private String describe;

    /**
     * 有效期类型 0: 永久有效 1: 用户自定义
     */
    private Integer validDateType;

    /**
     * 有效期
     */
    private Date validDate;
}
