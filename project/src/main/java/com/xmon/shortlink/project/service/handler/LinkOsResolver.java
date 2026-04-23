package com.xmon.shortlink.project.service.handler;

import org.springframework.stereotype.Component;

/**
 * 基于 User-Agent 解析访问操作系统。
 */
@Component
public class LinkOsResolver {

    /**
     * 解析请求操作系统；无法识别时返回 null。
     */
    public String resolve(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return null;
        }
        String normalizedUserAgent = userAgent.toLowerCase();
        if (normalizedUserAgent.contains("windows")) {
            return "Windows";
        }
        if (normalizedUserAgent.contains("android")) {
            return "Android";
        }
        if (normalizedUserAgent.contains("iphone")
                || normalizedUserAgent.contains("ipad")
                || normalizedUserAgent.contains("ipod")
                || normalizedUserAgent.contains("ios")) {
            return "iOS";
        }
        if (normalizedUserAgent.contains("mac os x")
                || normalizedUserAgent.contains("macintosh")) {
            return "macOS";
        }
        if (normalizedUserAgent.contains("linux")) {
            return "Linux";
        }
        return "Unknown";
    }
}
