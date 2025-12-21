package com.hongyuting.sports.service;

import com.hongyuting.sports.dto.AdminLoginDTO;
import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.Admin;
import com.hongyuting.sports.entity.AdminLog;
import lombok.Getter;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员服务接口
 */
public interface AdminService {
    /**
     * 管理员登录
     */
    ResponseDTO login(AdminLoginDTO loginDTO, String clientIP);

    /**
     * 管理员退出登录
     */
    ResponseDTO logout(Integer adminId, String token);

    /**
     * 刷新Token
     */
    ResponseDTO refreshAccessToken(String refreshToken);

    /**
     * 检查Token是否存在（用于拦截器验证）
     */
    boolean existsToken(String token);

    /**
     * 刷新Token过期时间
     */
    void refreshToken(String token);

    /**
     * 获取管理员信息（根据ID）
     */
    Admin getAdminById(Integer adminId);

    /**
     * 创建管理员（包含加盐加密）
     */
    @Transactional
    ResponseDTO createAdmin(Admin admin);

    /**
     * 修改管理员密码
     */
    ResponseDTO changePassword(Integer adminId, String oldPassword, String newPassword);

    /**
     * 获取管理员日志
     */
    List<AdminLog> getAdminLogs(Integer adminId, String operation, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据目标获取管理员日志
     */
    List<AdminLog> getAdminLogsByTarget(String targetType, Integer targetId);

    /**
     * 添加管理员日志
     */
    ResponseDTO addAdminLog(AdminLog adminLog);

    /**
     * 清理旧日志
     */
    ResponseDTO clearOldLogs(LocalDateTime beforeTime);

    /**
     * 获取管理员操作计数
     */
    Integer getAdminOperationCount(Integer adminId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取所有管理员
     */
    List<Admin> getAllAdmins();
    
    /**
     * 更新管理员信息
     */
    ResponseDTO updateAdminInfo(Admin admin);
    
    /**
     * 删除管理员
     */
    ResponseDTO deleteAdmin(Integer adminId);

    // 登录结果内部类
    @Getter
    @Setter
    class LoginResult {
        private Admin admin;
        private String token;
        private String refreshToken;
    }
}