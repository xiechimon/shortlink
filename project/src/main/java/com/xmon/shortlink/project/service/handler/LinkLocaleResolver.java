package com.xmon.shortlink.project.service.handler;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xmon.shortlink.project.dto.stats.LinkLocaleStatsInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 地区解析工具。
 * 根据客户端 IP 调用第三方地理位置服务，返回可用于统计落库的地区信息。
 */
@Slf4j
@Component
public class LinkLocaleResolver {

    @Value("${short-link.stats.locale.amap-key:}")
    private String amapKey;

    @Value("${short-link.stats.locale.amap-endpoint:https://restapi.amap.com/v3/ip}")
    private String amapEndpoint;

    @Value("${short-link.stats.locale.timeout-millis:3000}")
    private int timeoutMillis;

    @Value("${short-link.stats.locale.local-debug-enabled:false}")
    private boolean localDebugEnabled;

    @Value("${short-link.stats.locale.local-debug-province:Shanghai}")
    private String localDebugProvince;

    @Value("${short-link.stats.locale.local-debug-city:Shanghai}")
    private String localDebugCity;

    @Value("${short-link.stats.locale.local-debug-adcode:310000}")
    private String localDebugAdcode;

    @Value("${short-link.stats.locale.local-debug-country:CN}")
    private String localDebugCountry;

    /**
     * 解析客户端 IP 对应的地区信息。
     *
     * @param remoteAddr 客户端 IP
     * @return 地区统计信息；解析失败或配置缺失时返回空
     */
    public Optional<LinkLocaleStatsInfo> resolve(String remoteAddr) {
        if (remoteAddr == null || remoteAddr.isBlank()) {
            return Optional.empty();
        }
        if (localDebugEnabled && isLocalOrPrivateIp(remoteAddr)) {
            log.info("短链接地区解析使用本地联调兜底 remoteAddr={}", remoteAddr);
            return Optional.of(LinkLocaleStatsInfo.builder()
                    .province(normalize(localDebugProvince))
                    .city(normalize(localDebugCity))
                    .adcode(normalize(localDebugAdcode))
                    .country(normalize(localDebugCountry))
                    .build());
        }
        if (amapKey == null || amapKey.isBlank()) {
            log.warn("短链接地区解析跳过，未配置高德 Key remoteAddr={}", remoteAddr);
            return Optional.empty();
        }
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("key", amapKey);
            params.put("ip", remoteAddr);
            String response = HttpUtil.createGet(amapEndpoint)
                    .form(params)
                    .timeout(timeoutMillis)
                    .execute()
                    .body();
            JSONObject result = JSON.parseObject(response);
            if (result == null || !"1".equals(result.getString("status"))) {
                log.warn("短链接地区解析返回异常 remoteAddr={}, response={}", remoteAddr, response);
                return Optional.empty();
            }
            String province = normalize(result.getString("province"));
            String city = normalize(result.getString("city"));
            String adcode = normalize(result.getString("adcode"));
            if (province == null && city == null && adcode == null) {
                log.warn("短链接地区解析无有效地区信息 remoteAddr={}, response={}", remoteAddr, response);
                return Optional.empty();
            }
            return Optional.of(LinkLocaleStatsInfo.builder()
                    .province(province)
                    .city(city)
                    .adcode(adcode)
                    .country("CN")
                    .build());
        } catch (Exception ex) {
            log.warn("短链接地区解析失败 remoteAddr={}", remoteAddr, ex);
            return Optional.empty();
        }
    }

    private boolean isLocalOrPrivateIp(String remoteAddr) {
        try {
            InetAddress inetAddress = InetAddress.getByName(remoteAddr);
            return inetAddress.isAnyLocalAddress()
                    || inetAddress.isLoopbackAddress()
                    || inetAddress.isSiteLocalAddress();
        } catch (Exception ex) {
            log.warn("短链接地区解析无法识别 IP remoteAddr={}", remoteAddr, ex);
            return false;
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank() || "[]".equals(value)) {
            return null;
        }
        return value;
    }
}
