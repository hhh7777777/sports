package com.hongyuting.sports.mapper;

import com.hongyuting.sports.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
/**
 * 管理员映射接口
 */
@Mapper
public interface AdminMapper {
    /**
     * 根据用户名查询管理员
     */
    Admin findByUsername(String username);
    /**
     * 根据ID查询管理员
     */
    Admin findById(Integer adminId);
    /**
     * 添加管理员
     */
    int insertAdmin(Admin admin);
    /**
     * 更新管理员登录时间
     */
    void updateLastLoginTime(Integer adminId, LocalDateTime lastLoginTime);
    /**
     * 更新管理员密码
     */
    int updatePassword(Integer adminId, String password);
    /**
     * 更新管理员密码和盐
     */
    int updatePasswordAndSalt(@Param("adminId") Integer adminId, @Param("password") String password, @Param("salt") String salt);
    /**
     * 删除管理员
     */
    int deleteAdmin(Integer adminId);
    
    /**
     * 获取所有管理员
     */
    List<Admin> selectAllAdmins();
    
    /**
     * 更新管理员信息
     */
    int updateAdmin(Admin admin);

}