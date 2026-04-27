package com.xmon.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xmon.shortlink.project.dao.entity.LinkAccessStatsDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 访问统计持久层
 */
public interface LinkAccessStatsMapper extends BaseMapper<LinkAccessStatsDO> {

    /**
     * 记录基础访问监控数据
     */
    @Insert("""
            INSERT INTO t_link_access_stats
                (full_short_url, gid, date, pv, uv, uip, hour, weekday, create_time, update_time, del_flag)
            VALUES
                (#{linkAccessStats.fullShortUrl}, #{linkAccessStats.gid}, #{linkAccessStats.date}, #{linkAccessStats.pv}, #{linkAccessStats.uv}, #{linkAccessStats.uip}, #{linkAccessStats.hour}, #{linkAccessStats.weekday}, NOW(), NOW(), 0)
            ON DUPLICATE KEY UPDATE
                pv = pv + #{linkAccessStats.pv},
                uv = uv + #{linkAccessStats.uv},
                uip = uip + #{linkAccessStats.uip},
                update_time = NOW()
            """)
    void shortLinkStats(@Param("linkAccessStats") LinkAccessStatsDO linkAccessStatsDO);

    /**
     * 根据短链接和日期范围查询每日统计数据（用于每日列表、汇总、hour/weekday 分布）
     */
    @Select("""
            SELECT date, pv, uv, uip, hour, weekday
            FROM t_link_access_stats
            WHERE full_short_url = #{fullShortUrl}
              AND gid = #{gid}
              AND date >= #{startDate}
              AND date <= #{endDate}
              AND del_flag = 0
            ORDER BY date ASC, hour ASC
            """)
    List<LinkAccessStatsDO> listStatsByLink(
            @Param("fullShortUrl") String fullShortUrl,
            @Param("gid") String gid,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );
}
