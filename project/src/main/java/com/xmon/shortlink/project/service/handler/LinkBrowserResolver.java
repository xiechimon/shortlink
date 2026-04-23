package com.xmon.shortlink.project.service.handler;

import org.springframework.stereotype.Component;

/**
 * 基于 User-Agent 解析访问浏览器。
 */
@Component
public class LinkBrowserResolver {

    /**
     * 解析请求浏览器；无法识别时返回 null。
     */
    public String resolve(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return null;
        }
        String normalizedUserAgent = userAgent.toLowerCase();
        if (normalizedUserAgent.contains("edg/")) {
            return "Edge";
        }
        if (normalizedUserAgent.contains("opr/")
                || normalizedUserAgent.contains("opera")) {
            return "Opera";
        }
        if (normalizedUserAgent.contains("firefox/")) {
            return "Firefox";
        }
        if (normalizedUserAgent.contains("chrome/")) {
            return "Chrome";
        }
        if (normalizedUserAgent.contains("safari/")
                && !normalizedUserAgent.contains("chrome/")
                && !normalizedUserAgent.contains("chromium/")) {
            return "Safari";
        }
        if (normalizedUserAgent.contains("msie")
                || normalizedUserAgent.contains("trident/")) {
            return "IE";
        }
        return "Unknown";
    }
}
