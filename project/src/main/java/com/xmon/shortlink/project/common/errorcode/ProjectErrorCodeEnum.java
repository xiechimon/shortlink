package com.xmon.shortlink.project.common.errorcode;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 项目错误码枚举
 */
@Getter
@AllArgsConstructor
public enum ProjectErrorCodeEnum {

    LINK_NOT_FOUND("P000001", "短链接不存在"),
    LINK_ENABLE_FAILED("P000002", "短链接启用失败"),
    LINK_DISABLE_FAILED("P000003", "短链接禁用失败"),
    ;

    private final String code;
    private final String message;
}
