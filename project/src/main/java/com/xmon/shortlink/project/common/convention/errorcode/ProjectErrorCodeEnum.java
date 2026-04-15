package com.xmon.shortlink.project.common.convention.errorcode;

import lombok.RequiredArgsConstructor;

/**
 * 项目错误码枚举
 */
@RequiredArgsConstructor
public enum ProjectErrorCodeEnum implements IErrorCode {

    LINK_EXIST("A000300", "短链接已存在"),
    LINK_NOT_FOUND("A000301", "短链接不存在"),
    LINK_ENABLE_FAILED("A000302", "短链接启用失败"),
    LINK_DISABLE_FAILED("A000303", "短链接禁用失败"),

    LINK_GENERATE_TOO_MANY("B000300", "短链接生成频繁，请稍后再试"),
    ;

    private final String code;
    private final String message;

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
