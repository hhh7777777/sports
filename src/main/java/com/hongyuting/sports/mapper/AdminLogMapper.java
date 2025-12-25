package com.hongyuting.sports.mapper;

import com.hongyuting.sports.entity.AdminLog;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员日志映射接口
 */
public interface AdminLogMapper extends BaseMapper<AdminLog, Integer> {

    /**
     * 根据管理员ID查询日志
     */
    List<AdminLog> selectByAdminId(@Param("adminId") Integer adminId);

    /**
     * 根据操作类型查询日志
     */
    List<AdminLog> selectByOperation(@Param("operation") String operation);

    /**
     * 根据条件查询日志
     */
    List<AdminLog> selectByCondition(@Param("adminId") Integer adminId,
                                     @Param("operation") String operation,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime,
                                     @Param("logLevel") String logLevel);

    /**
     * 根据目标查询日志
     */
    List<AdminLog> selectByTarget(@Param("targetType") String targetType,
                                  @Param("targetId") Integer targetId);

    /**
     * 统计操作次数
     */
    Integer selectOperationCount(@Param("adminId") Integer adminId,
                                 @Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 删除指定时间前的日志
     */
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);
    
    /**
     * 根据管理员ID删除日志
     */
    void deleteByAdminId(@Param("adminId") Integer adminId);
}