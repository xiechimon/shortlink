package com.xmon.shortlink.project.service.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.net.NetUtil;
import org.springframework.stereotype.Component;

/**
 * 解析访问网络。
 */
@Component
public class LinkNetworkResolver {

    /**
     * 解析请求网络；无法从User-Agent识别时，若为内网IP则默认返回 WIFI。
     */
    public String resolve(String actualIp, String userAgent) {
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

        // 解析不到则判断是否是内网IP，如果是则默认 WIFI
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
