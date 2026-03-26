package com.xmon.shortlink.admin.common.enums;

import com.xmon.shortlink.admin.common.convention.errorcode.IErrorCode;

/**
 * 用户模块错误码
 */
public enum UserErrorCodeEnum implements IErrorCode {

    USER_NOT_FOUND("A000002", "用户不存在"),
    USER_NAME_EXIST("A000003", "用户名已存在"),
    USER_PASSWORD_INVALID("A000004", "用户名或密码错误"),
    USER_DISABLED("A000005", "用户已被禁用");

    private final String code;
    private final String message;

    UserErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
