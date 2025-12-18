package com.hongyuting.sports.mapper;

import com.hongyuting.sports.entity.User;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper {
    int selectUserCount();

    int insertUser(User user);

    int updateUser(User user);

    int disableUser(@Param("userId") int userId);

    User selectUserById(@Param("userId") int userId);

    User selectUserByUsername(@Param("username") String username);

    User selectUserByWxOpenid(@Param("openid") String openid);

    List<User> selectAllUsers();

    int updateLastLoginTime(@Param("userId") int userId, @Param("loginTime") LocalDateTime loginTime);

    int countUsers();
    int deleteUser(@Param("userId") Integer userId);


    int updateUserPassword(@Param("userId") Integer userId,
                           @Param("password") String password,
                           @Param("salt") String salt);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User selectUserByEmail(@Param("email") String email);


}