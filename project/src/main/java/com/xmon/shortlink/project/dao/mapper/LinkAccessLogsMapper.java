package com.xmon.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xmon.shortlink.project.dao.entity.LinkAccessLogsDO;
import com.xmon.shortlink.project.dto.resp.ShortLinkStatsTopRespVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 访问日志持久层
 */
public interface LinkAccessLogsMapper extends BaseMapper<LinkAccessLogsDO> {

    /**
     * 查询日期范围内在此之前已经有过访问记录的老访客用户标识列表
     * （用于区分新老访客：在 startDate 之前就存在于日志中的 user）
     */
    @Select("""
            SELECT DISTINCT user
            FROM t_link_access_logs
            WHERE full_short_url = #{fullShortUrl}
              AND gid = #{gid}
              AND del_flag = 0
              AND DATE(create_time) < #{startDate}
            """)
    List<String> listOldUsersByLink(
            @Param("fullShortUrl") String fullShortUrl,
            @Param("gid") String gid,
            @Param("startDate") String startDate
    );

    /**
     * 查询日期范围内所有独立访客用户标识列表
     */
    @Select("""
            SELECT DISTINCT user
            FROM t_link_access_logs
            WHERE full_short_url = #{fullShortUrl}
              AND gid = #{gid}
              AND del_flag = 0
              AND DATE(create_time) >= #{startDate}
              AND DATE(create_time) <= #{endDate}
            """)
    List<String> listUvUsersByLink(
            @Param("fullShortUrl") String fullShortUrl,
            @Param("gid") String gid,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    /**
     * 查询日期范围内访问次数最高的 IP 列表
     */
    @Select("""
            SELECT ip AS value, COUNT(*) AS cnt
            FROM t_link_access_logs
            WHERE full_short_url = #{fullShortUrl}
              AND gid = #{gid}
              AND del_flag = 0
              AND DATE(create_time) >= #{startDate}
              AND DATE(create_time) <= #{endDate}
              AND ip IS NOT NULL
              AND ip != ''
            GROUP BY ip
            ORDER BY cnt DESC
            LIMIT 5
            """)
    List<ShortLinkStatsTopRespVO> listTopIpStatsByLink(
            @Param("fullShortUrl") String fullShortUrl,
            @Param("gid") String gid,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );
}
