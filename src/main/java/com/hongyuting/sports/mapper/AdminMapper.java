package com.hongyuting.sports.mapper;

import com.hongyuting.sports.entity.Admin;
import com.hongyuting.sports.entity.AdminLog;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
/**
 * 管理员映射接口
 */
public interface AdminMapper {

    Admin findByUsername(String username);

    Admin findById(Integer id);

    int insertAdmin(Admin admin);

    void updateLastLoginTime(Integer id, LocalDateTime lastLoginTime);

    int updatePassword(Integer id, String password);
    
    int updatePasswordAndSalt(@Param("id") Integer id, @Param("password") String password, @Param("salt") String salt);

    // AdminLog相关方法
    int insertAdminLog(AdminLog adminLog);

    List<AdminLog> findAdminLogs(@Param("adminId") Integer adminId, 
                                @Param("operation") String operation, 
                                @Param("startTime") LocalDateTime startTime, 
                                @Param("endTime") LocalDateTime endTime);
    
    List<AdminLog> findAdminLogsByTarget(@Param("targetType") String targetType, 
                                        @Param("targetId") Integer targetId);
    
    int deleteAdminLogsBefore(LocalDateTime beforeTime);
    
    Integer selectOperationCount(@Param("adminId") Integer adminId, 
                               @Param("startTime") LocalDateTime startTime, 
                               @Param("endTime") LocalDateTime endTime);
}