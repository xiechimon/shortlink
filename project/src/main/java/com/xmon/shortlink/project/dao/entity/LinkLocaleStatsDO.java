package com.xmon.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xmon.shortlink.project.common.database.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 实体类：短链接地区访问统计。
 * 记录某个短链接在某天、某地区的访问次数，用于后续地区维度报表。
 */
@Data
@Builder
@TableName("t_link_locale_stats")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LinkLocaleStatsDO extends BaseDO {

    /**
     * ID
     */
    private Long id;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 日期
     */
    private Date date;

    /**
     * 访问量
     */
    private Integer cnt;

    /**
     * 省份名称
     */
    private String province;

    /**
     * 城市名称
     */
    private String city;

    /**
     * 行政区划编码
     */
    private String adcode;

    /**
     * 国家名称
     */
    private String country;
}
