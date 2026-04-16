package com.xmon.shortlink.project.common.constant;

import java.util.concurrent.TimeUnit;

/**
 * 短链接缓存策略常量
 */
public final class ShortLinkCacheConstant {

    private ShortLinkCacheConstant() {
    }

    /**
     * 永久短链接默认缓存有效期（1 个月，毫秒）
     */
    public static final long DEFAULT_CACHE_VALID_TIME = TimeUnit.DAYS.toMillis(30);
}
