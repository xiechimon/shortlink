package com.xmon.shortlink.project.common.database;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据库持久层对象基础属性
 */
@Data
public class BaseDO {
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标识 0：未删除 1：已删除
     * TODO: 最佳实践：建议后续在此处补充 @TableLogic 注解，配合全局逻辑删除配置，
     * 以便实现所有 SELECT/UPDATE/DELETE 操作自动化带上 AND del_flag=0，消灭大量硬编码。
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;
}
