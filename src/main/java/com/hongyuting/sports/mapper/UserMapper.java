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
    int updateUser(User user);
    int updateUserAvatar(User user);
    User selectUserById(Integer userId);
    User selectUserByUsername(String username);
    List<User> selectAllUsers();
    List<User> searchUsers(@Param("username") String username, @Param("email") String email, @Param("status") Integer status);
    int countByUsername(String username);
    int countByEmail(String email);
    int deleteUser(Integer userId);
    
    // 新增方法
    int selectUserCountByMonth(@Param("year") int year, @Param("month") int month);
    Integer selectTotalDurationByUserAndDate(@Param("userId") Integer userId, 
                                          @Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
    List<Map<String, Object>> selectBehaviorTypeDistribution(@Param("userId") Integer userId,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);
    
    // 添加获取指定类型和日期范围内行为记录数的方法
    Integer countBehaviorRecordsByTypeAndDate(@Param("userId") Integer userId,
                                             @Param("typeName") String typeName,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);
}