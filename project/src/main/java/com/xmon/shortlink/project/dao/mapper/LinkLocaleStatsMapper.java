package com.xmon.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xmon.shortlink.project.dao.entity.LinkLocaleStatsDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 地区访问统计持久层
 */
public interface LinkLocaleStatsMapper extends BaseMapper<LinkLocaleStatsDO> {

    /**
     * 记录地区访问统计数据
     */
    @Insert("""
            INSERT INTO t_link_locale_stats
                (full_short_url, gid, date, cnt, province, city, adcode, country, create_time, update_time, del_flag)
            VALUES
                (#{linkLocaleStats.fullShortUrl}, #{linkLocaleStats.gid}, #{linkLocaleStats.date}, #{linkLocaleStats.cnt}, #{linkLocaleStats.province}, #{linkLocaleStats.city}, #{linkLocaleStats.adcode}, #{linkLocaleStats.country}, NOW(), NOW(), 0)
            ON DUPLICATE KEY UPDATE
                cnt = cnt + #{linkLocaleStats.cnt},
                update_time = NOW()
            """)
    void shortLinkLocaleStats(@Param("linkLocaleStats") LinkLocaleStatsDO linkLocaleStatsDO);

    /**
     * 查询地区维度统计（按日期范围聚合，按省级分组）
     */
    @Select("""
            SELECT province, SUM(cnt) AS cnt
            FROM t_link_locale_stats
            WHERE full_short_url = #{fullShortUrl}
              AND gid = #{gid}
              AND date >= #{startDate}
              AND date <= #{endDate}
              AND del_flag = 0
            GROUP BY province
            """)
    List<LinkLocaleStatsDO> listStatsByLink(
            @Param("fullShortUrl") String fullShortUrl,
            @Param("gid") String gid,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );
}
