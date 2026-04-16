package com.xmon.shortlink.project.common.constant;

/**
 * 短链接项目 Redis 缓存相关常量
 */
public final class RedisCacheConstant {

    private RedisCacheConstant() {
    }

    /**
     * 短链接跳转缓存 Key 模板
     * 示例：short-link:goto:127.0.0.1:8001/AbCd12
     */
    public static final String GOTO_SHORT_LINK_KEY = "short-link:goto:%s";

    /**
     * 短链接跳转重建缓存锁 Key 模板
     * 与跳转缓存 key 一一对应，避免不同短链接互相阻塞。
     */
    public static final String GOTO_SHORT_LINK_LOCK_KEY = "short-link:goto:lock:%s";

    /**
     * 短链接空值标记 Key 模板
     * 用于标记不存在的短链接，防止缓存穿透反复回源。
     */
    public static final String GOTO_IS_NULL_SHORT_LINK_KEY = "short-link:goto:is-null:%s";

    /**
     * 短链接不存在的占位值
     * 用于缓存穿透保护：命中该值直接返回 404，不再回源数据库。
     */
    public static final String GOTO_LINK_NULL_VALUE = "-";

    /**
     * 空值缓存有效期（秒）
     * 保持较短 TTL，避免真实数据刚写入后长期被空值缓存遮蔽。
     */
    public static final long GOTO_LINK_NULL_TTL_SECONDS = 30L;

    /**
     * 默认短链接缓存有效期（天）
     * 当链接为永久有效或未配置精确过期时间时，采用默认缓存时长。
     */
    public static final long GOTO_LINK_DEFAULT_TTL_DAYS = 1L;

    /**
     * 构建短链接跳转缓存 key，统一 key 生成逻辑，避免业务层散落 String.format。
     */
    public static String buildGotoShortLinkKey(String fullShortUrl) {
        return String.format(GOTO_SHORT_LINK_KEY, fullShortUrl);
    }

    /**
     * 构建短链接跳转分布式锁 key，确保与缓存 key 维度一致。
     */
    public static String buildGotoShortLinkLockKey(String fullShortUrl) {
        return String.format(GOTO_SHORT_LINK_LOCK_KEY, fullShortUrl);
    }

    /**
     * 构建短链接空值标记 key。
     */
    public static String buildGotoIsNullShortLinkKey(String fullShortUrl) {
        return String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl);
    }
}
