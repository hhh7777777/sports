package com.hongyuting.sports.mapper;

import com.hongyuting.sports.entity.AdminLog;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminLogMapper {
    /**
     * 插入管理员日志
     */
    int insertAdminLog(AdminLog adminLog);

    /**
     * 根据ID查询管理员日志
     */
    AdminLog selectAdminLogById(Integer logId);

    /**
     * 根据管理员ID查询日志
     */
    List<AdminLog> selectAdminLogsByAdmin(Integer adminId);

    /**
     * 根据操作类型查询日志
     */
    List<AdminLog> selectAdminLogsByOperation(String operation);

    /**
     * 统计日志数量
     */
    int countAdminLogs();

    /**
     * 根据条件查询日志
     */
    List<AdminLog> selectAdminLogs(@Param("adminId") Integer adminId,
                                   @Param("operation") String operation,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 根据目标查询日志
     */
    List<AdminLog> selectAdminLogsByTarget(@Param("targetType") String targetType,
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
    int deleteAdminLogsBefore(LocalDateTime beforeTime);
}