package com.xmon.shortlink.project.service.handler;

import org.springframework.stereotype.Component;

/**
 * 基于 User-Agent 解析访问设备。
 */
@Component
public class LinkDeviceResolver {

    /**
     * 解析请求设备；默认为 PC，识别到移动设备关键字则为 Mobile。
     */
    public String resolve(String userAgent) {
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
}
