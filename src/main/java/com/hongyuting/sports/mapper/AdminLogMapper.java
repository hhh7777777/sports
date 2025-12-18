package com.hongyuting.sports.mapper;

import com.hongyuting.sports.entity.AdminLog;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AdminLogMapper {
    int insertAdminLog(AdminLog adminLog);
    AdminLog selectAdminLogById(@Param("logId") long logId);
    List<AdminLog> selectAdminLogsByAdmin(@Param("adminId") int adminId);
    List<AdminLog> selectAdminLogsByOperation(@Param("operation") String operation);
    int countAdminLogs();
    int deleteAdminLogsBefore(LocalDateTime beforeTime);

    List<AdminLog> selectAdminLogs(@Param("adminId") Integer adminId,
                                   @Param("operation") String operation,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    List<AdminLog> selectAdminLogsByTarget(@Param("targetType") String targetType,
                                           @Param("targetId") Integer targetId);

    int selectOperationCount(@Param("adminId") Integer adminId,
                             @Param("startTime") LocalDateTime startTime,
                             @Param("endTime") LocalDateTime endTime);
}