package com.xmon.shortlink.admin.toolkit;

import java.security.SecureRandom;

/**
 * 随机字符串生成工具类
 */
public final class RandomStringUtil {

    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();

    private RandomStringUtil() {
    }

    /**
     * 生成指定长度的随机字符串（包含数字和大小写字母）
     *
     * @param length 长度
     * @return 随机字符串
     */
    public static String generate(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    /**
     * 生成6位随机字符串
     *
     * @return 6位随机字符串
     */
    public static String generate() {
        return generate(6);
    }
}
