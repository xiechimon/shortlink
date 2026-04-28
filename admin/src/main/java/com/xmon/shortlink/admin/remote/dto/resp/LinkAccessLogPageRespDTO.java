package com.xmon.shortlink.admin.remote.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 短链接访问日志分页返回参数（Admin 远程调用）
 */
@Data
public class LinkAccessLogPageRespDTO {

    /**
     * ID
     */
    private Long id;

    /**
     * 用户标识
     */
    private String user;

    /**
     * 访客类型（newUser / oldUser）
     */
    private String uvType;

    /**
     * IP
     */
    private String ip;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 访问网络
     */
    private String network;

    /**
     * 访问设备
     */
    private String device;

    /**
     * 地区
     */
    private String locale;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
