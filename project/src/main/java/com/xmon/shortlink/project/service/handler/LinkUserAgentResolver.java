package com.xmon.shortlink.project.service.handler;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;

@Component
public class LinkUserAgentResolver {

    public String resolveBrowser(String userAgent) {
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

    public String resolveDevice(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "PC";
        }
        String normalizedUserAgent = userAgent.toLowerCase();
        if (normalizedUserAgent.contains("mobile") ||
                normalizedUserAgent.contains("android") ||
                normalizedUserAgent.contains("iphone") ||
                normalizedUserAgent.contains("ipad") ||
                normalizedUserAgent.contains("ipod") ||
                normalizedUserAgent.contains("windows phone") ||
                normalizedUserAgent.contains("kindle") ||
                normalizedUserAgent.contains("silk/") ||
                normalizedUserAgent.contains("playbook") ||
                normalizedUserAgent.contains("blackberry") ||
                normalizedUserAgent.contains("bb10") ||
                normalizedUserAgent.contains("opera mini")) {
            return "Mobile";
        }
        return "PC";
    }

    public String resolveOs(String userAgent) {
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

    public String resolveNetwork(String actualIp, String userAgent) {
        String normalizedUserAgent = StrUtil.isBlank(userAgent) ? "" : userAgent.toLowerCase();
        if (normalizedUserAgent.contains("wifi")) {
            return "WIFI";
        }
        if (normalizedUserAgent.contains("5g")) {
            return "5G";
        }
        if (normalizedUserAgent.contains("4g") || normalizedUserAgent.contains("lte")) {
            return "4G";
        }
        if (normalizedUserAgent.contains("3g")) {
            return "3G";
        }
        if (normalizedUserAgent.contains("2g")) {
            return "2G";
        }

        if (StrUtil.isNotBlank(actualIp)) {
            if (actualIp.startsWith("127.") || actualIp.startsWith("192.168.") || actualIp.startsWith("10.") || actualIp.startsWith("172.")) {
                return "WIFI";
            }
            try {
                if (NetUtil.isInnerIP(actualIp)) {
                    return "WIFI";
                }
            } catch (Exception ignored) {
            }
        }

        return "Unknown";
    }
}
