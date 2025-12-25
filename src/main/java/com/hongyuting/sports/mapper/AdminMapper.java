package com.hongyuting.sports.mapper;

import com.hongyuting.sports.entity.Admin;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

/**
 * 管理员映射接口
 */
public interface AdminMapper extends BaseMapper<Admin, Integer> {

    /**
     * 根据用户名查询管理员
     */
    Admin findByUsername(String username);

    /**
     * 更新管理员登录时间
     */
    void updateLastLoginTime(@Param("adminId") Integer adminId,
                             @Param("lastLoginTime") LocalDateTime lastLoginTime);

    /**
     * 更新管理员密码
     */
    int updatePassword(@Param("adminId") Integer adminId,
                       @Param("password") String password);

    /**
     * 更新管理员密码和盐
     */
    int updatePasswordAndSalt(@Param("adminId") Integer adminId,
                              @Param("password") String password,
                              @Param("salt") String salt);

    /**
     * 更新管理员信息（不包含密码）
     */
    int updateAdminInfo(Admin admin);
}