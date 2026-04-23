package com.xmon.shortlink.project.tookit;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 客户端 IP 工具类
 */
public final class ClientIpUtil {

    private static final List<String> CLIENT_IP_HEADER_CANDIDATES = List.of(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "X-Real-IP"
    );

    private ClientIpUtil() {
    }

    public static String getActualIp(HttpServletRequest request) {
        for (String header : CLIENT_IP_HEADER_CANDIDATES) {
            String headerValue = request.getHeader(header);
            if (headerValue == null || headerValue.isBlank() || "unknown".equalsIgnoreCase(headerValue)) {
                continue;
            }
            String clientIp = headerValue.split(",")[0].trim();
            if (!clientIp.isBlank() && !"unknown".equalsIgnoreCase(clientIp)) {
                return clientIp;
            }
        }
        String remoteAddr = request.getRemoteAddr();
        return (remoteAddr == null || remoteAddr.isBlank()) ? null : remoteAddr;
    }
}
