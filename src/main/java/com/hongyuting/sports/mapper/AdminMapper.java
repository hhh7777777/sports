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
    /**
     * 根据用户名查询管理员
     */
    Admin findByUsername(String username);
    /**
     * 根据ID查询管理员
     */
    Admin findById(Integer id);
    /**
     * 添加管理员
     */
    int insertAdmin(Admin admin);
    /**
     * 更新管理员登录时间
     */
    void updateLastLoginTime(Integer id, LocalDateTime lastLoginTime);
    /**
     * 更新管理员密码
     */
    int updatePassword(Integer id, String password);
    /**
     * 更新管理员密码和盐
     */
    int updatePasswordAndSalt(@Param("id") Integer id, @Param("password") String password, @Param("salt") String salt);
    /**
     * 删除管理员
     */
    // AdminLog相关方法
    int insertAdminLog(AdminLog adminLog);

    List<AdminLog> findAdminLogs(@Param("adminId") Integer adminId, 
                                @Param("operation") String operation, 
                                @Param("startTime") LocalDateTime startTime, 
                                @Param("endTime") LocalDateTime endTime);
    /**
     * 根据目标查询管理员操作日志
     */
    List<AdminLog> findAdminLogsByTarget(@Param("targetType") String targetType, 
                                        @Param("targetId") Integer targetId);
    /**
     * 删除管理员操作日志
     */
    int deleteAdminLogsBefore(LocalDateTime beforeTime);
    /**
     * 获取管理员操作日志数量
     */
    Integer selectOperationCount(@Param("adminId") Integer adminId, 
                               @Param("startTime") LocalDateTime startTime, 
                               @Param("endTime") LocalDateTime endTime);
}