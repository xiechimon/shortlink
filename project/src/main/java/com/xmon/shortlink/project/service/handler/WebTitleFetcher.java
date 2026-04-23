package com.xmon.shortlink.project.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

/**
 * 目标网站信息抓取工具（基于 Jsoup实现）
 */
@Slf4j
@Component
public class WebTitleFetcher {

    private static final int TIMEOUT_MS = 3000;

    /**
     * 获取目标 URL 的网页标题
     *
     * @param originUrl 目标链接
     * @return 网页标题；若无法获取则返回 null
     */
    public String fetchTitle(String originUrl) {
        if (originUrl == null || originUrl.isBlank()) {
            return null;
        }
        if (!originUrl.toLowerCase().startsWith("http://") && !originUrl.toLowerCase().startsWith("https://")) {
            originUrl = "http://" + originUrl;
        }

        try {
            Document document = Jsoup.connect(originUrl)
                    .timeout(TIMEOUT_MS)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,*/*;q=0.9")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .get();

            String title = document.title();

            if (!title.isBlank()) {
                return title.trim();
            }
        } catch (Exception e) {
            log.warn("[WebTitleFetcher] 获取 {} 网页标题失败：{}", originUrl, e.getMessage());
        }
        return null;
    }

    /**
     * 获取目标 URL 的网页图标 (Favicon)
     *
     * @param originUrl 目标链接
     * @return 网页图标绝对地址；若无法获取则返回默认回退地址
     */
    public String fetchFavicon(String originUrl) {
        if (originUrl == null || originUrl.isBlank()) {
            return null;
        }
        if (!originUrl.toLowerCase().startsWith("http://") && !originUrl.toLowerCase().startsWith("https://")) {
            originUrl = "http://" + originUrl;
        }

        try {
            Document document = Jsoup.connect(originUrl)
                    .timeout(TIMEOUT_MS)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                    .get();

            org.jsoup.nodes.Element iconElement = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (iconElement != null) {
                String faviconUrl = iconElement.attr("abs:href");
                if (!faviconUrl.isBlank()) {
                    return faviconUrl;
                }
            }

            iconElement = document.select("link[rel~=(?i)^apple-touch-icon]").first();
            if (iconElement != null) {
                String faviconUrl = iconElement.attr("abs:href");
                if (!faviconUrl.isBlank()) {
                    return faviconUrl;
                }
            }

        } catch (Exception e) {
            log.warn("[WebTitleFetcher] 获取 {} 网页图标失败，执行回退策略：{}", originUrl, e.getMessage());
        }

        try {
            java.net.URL parsedUrl = new java.net.URL(originUrl);
            return String.format("%s://%s/favicon.ico", parsedUrl.getProtocol(), parsedUrl.getHost());
        } catch (Exception ignored) {
        }

        return null;
    }
}
