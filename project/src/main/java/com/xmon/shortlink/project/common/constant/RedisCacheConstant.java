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
     * 短链接 UV 统计 Redis Key 模板
     * 格式：short-link:stats:uv:{fullShortUrl}:{date}
     * 类型：Set，存储访问过该短链接的所有 UV Cookie 值。
     * TTL：25 小时，保证跨天后自然过期。
     */
    public static final String STATS_UV_SHORT_LINK_KEY = "short-link:stats:uv:%s:%s";

    /**
     * 短链接 UIP 统计 Redis Key 模板
     * 格式：short-link:stats:uip:{fullShortUrl}:{date}
     * 类型：Set，存储访问过该短链接的所有客户端 IP。
     * TTL：25 小时，保证跨天后自然过期。
     */
    public static final String STATS_UIP_SHORT_LINK_KEY = "short-link:stats:uip:%s:%s";

    /**
     * 短链接 UV 统计 Cookie 名称
     */
    public static final String STATS_UV_COOKIE_NAME = "uv";

    /**
     * 短链接 UV 统计 Cookie 有效期（秒）：1 个月
     */
    public static final int STATS_UV_COOKIE_MAX_AGE = 60 * 60 * 24 * 30;

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

    /**
     * 短链接 gid 缓存 Key 模板
     */
    public static final String GOTO_SHORT_LINK_GID_KEY = "short-link:goto:gid:%s";

    /**
     * 构建短链接 UV 统计 Redis key（以短链接 + 日期为维度）。
     */
    public static String buildStatsUvKey(String fullShortUrl, String date) {
        return String.format(STATS_UV_SHORT_LINK_KEY, fullShortUrl, date);
    }

    /**
     * 构建短链接 UIP 统计 Redis key（以短链接 + 日期为维度）。
     */
    public static String buildStatsUipKey(String fullShortUrl, String date) {
        return String.format(STATS_UIP_SHORT_LINK_KEY, fullShortUrl, date);
    }

    public static String buildGotoShortLinkGidKey(String fullShortUrl) {
        return String.format(GOTO_SHORT_LINK_GID_KEY, fullShortUrl);
    }
}
