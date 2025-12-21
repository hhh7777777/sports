package com.hongyuting.sports.mapper;

import com.hongyuting.sports.entity.User;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
    
    Integer selectTotalDurationByUserAndDate(@Param("userId") Integer userId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);
                                             
    List<Map<String, Object>> selectBehaviorTypeDistribution(@Param("userId") Integer userId,
                                                             @Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);
}