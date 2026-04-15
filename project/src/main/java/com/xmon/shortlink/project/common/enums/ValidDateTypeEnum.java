package com.xmon.shortlink.project.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 短链接有效期类型枚举
 */
@Getter
@RequiredArgsConstructor
public enum ValidDateTypeEnum {

    PERMANENT(0, "永久有效"),

    CUSTOM(1, "用户自定义");

    private final Integer type;
    private final String description;
}
