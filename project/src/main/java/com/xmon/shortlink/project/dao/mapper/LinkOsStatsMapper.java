package com.xmon.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xmon.shortlink.project.dao.entity.LinkOsStatsDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作系统访问统计持久层
 */
public interface LinkOsStatsMapper extends BaseMapper<LinkOsStatsDO> {

    /**
     * 记录操作系统访问统计数据
     */
    @Insert("""
            INSERT INTO t_link_os_stats
                (full_short_url, gid, date, cnt, os, create_time, update_time, del_flag)
            VALUES
                (#{linkOsStats.fullShortUrl}, #{linkOsStats.gid}, #{linkOsStats.date}, #{linkOsStats.cnt}, #{linkOsStats.os}, NOW(), NOW(), 0)
            ON DUPLICATE KEY UPDATE
                cnt = cnt + #{linkOsStats.cnt},
                update_time = NOW()
            """)
    void shortLinkOsStats(@Param("linkOsStats") LinkOsStatsDO linkOsStatsDO);

    /**
     * 查询 OS 维度统计（按日期范围聚合）
     */
    @Select("""
            SELECT os, SUM(cnt) AS cnt
            FROM t_link_os_stats
            WHERE full_short_url = #{fullShortUrl}
              AND gid = #{gid}
              AND date >= #{startDate}
              AND date <= #{endDate}
              AND del_flag = 0
            GROUP BY os
            """)
    List<LinkOsStatsDO> listStatsByLink(
            @Param("fullShortUrl") String fullShortUrl,
            @Param("gid") String gid,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );
}
