package com.xmon.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xmon.shortlink.project.dao.entity.LinkNetworkStatsDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 接口：短链接网络统计持久层。
 */
public interface LinkNetworkStatsMapper extends BaseMapper<LinkNetworkStatsDO> {

    /**
     * 记录网络访问监控数据
     */
    @Insert("INSERT INTO t_link_network_stats (full_short_url, gid, date, cnt, network, create_time, update_time, del_flag) " +
            "VALUES(#{linkNetworkStats.fullShortUrl}, #{linkNetworkStats.gid}, #{linkNetworkStats.date}, #{linkNetworkStats.cnt}, #{linkNetworkStats.network}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt +  #{linkNetworkStats.cnt};")
    void shortLinkNetworkState(@Param("linkNetworkStats") LinkNetworkStatsDO linkNetworkStatsDO);

    /**
     * 查询网络维度统计（按日期范围聚合）
     */
    @Select("""
            SELECT network, SUM(cnt) AS cnt
            FROM t_link_network_stats
            WHERE full_short_url = #{fullShortUrl}
              AND gid = #{gid}
              AND date >= #{startDate}
              AND date <= #{endDate}
              AND del_flag = 0
            GROUP BY network
            """)
    List<LinkNetworkStatsDO> listStatsByLink(
            @Param("fullShortUrl") String fullShortUrl,
            @Param("gid") String gid,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );
}
