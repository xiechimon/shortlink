package com.xmon.shortlink.admin.common.constant;

/**
 * 短链接后台系统 Redis 缓存相关常量类
 */
public class RedisCacheConstant {

    /**
     * 用户注册分布式锁
     */
    public static final String LOCK_USER_REGISTER_KEY = "rshortlink:lock_user-register:";

    /**
     * 用户登录缓存 Key 前缀，格式：login:{username}:{token}
     */
    public static final String USER_LOGIN_KEY = "login:";

    /**
     * 用户登录缓存有效期（分钟）
     */
    public static final long USER_LOGIN_TIMEOUT = 3000000L;
}
