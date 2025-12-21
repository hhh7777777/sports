package com.hongyuting.sports.mapper;

import com.hongyuting.sports.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户映射接口
 */
public interface UserMapper {
    int insertUser(User user);

    User selectUserByUsername(String username);

    User selectUserById(Integer userId);

    int updateUser(User user);

    int updateUserAvatar(User user);

    int updateUserStatus(User user);

    int deleteUser(Integer userId);

    List<User> selectAllUsers();

    int countByUsername(String username);

    int countByEmail(String email);

}
