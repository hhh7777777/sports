package com.hongyuting.sports.mapper;

import com.hongyuting.sports.entity.AdminLog;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
/**
 * 管理员操作日志映射接口
 */
public interface AdminLogMapper {
    /**
     * 添加管理员操作日志
     */
    int insertAdminLog(AdminLog adminLog);
    /**
     * 根据ID查询管理员操作日志
     */
    AdminLog selectAdminLogById(@Param("logId") long logId);
    /**
     * 根据管理员ID查询管理员操作日志
     */
    List<AdminLog> selectAdminLogsByAdmin(@Param("adminId") int adminId);
    /**
     * 根据操作查询管理员操作日志
     */
    List<AdminLog> selectAdminLogsByOperation(@Param("operation") String operation);
    /**
     * 获取管理员操作日志总数
     */
    int countAdminLogs();
    /**
     * 删除管理员操作日志
     */
    int deleteAdminLogsBefore(LocalDateTime beforeTime);
    /**
     * 查询管理员操作日志
     */
    List<AdminLog> selectAdminLogs(@Param("adminId") Integer adminId,
                                   @Param("operation") String operation,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);
    /**
     * 根据目标查询管理员操作日志
     */
    List<AdminLog> selectAdminLogsByTarget(@Param("targetType") String targetType,
                                           @Param("targetId") Integer targetId);
    /**
     * 获取管理员操作日志数量
     */
    int selectOperationCount(@Param("adminId") Integer adminId,
                             @Param("startTime") LocalDateTime startTime,
                             @Param("endTime") LocalDateTime endTime);
}