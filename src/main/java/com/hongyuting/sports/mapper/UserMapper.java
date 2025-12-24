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
    /**
     * 添加用户
     */
    int insertUser(User user);
    /**
     * 更新用户
     */
    int updateUser(User user);
    /**
     * 更新用户头像
     */
    int updateUserAvatar(User user);
    /**
     * 根据ID查询用户
     */
    User selectUserById(Integer userId);
    /**
     * 根据用户名查询用户
     */
    User selectUserByUsername(String username);
    /**
     * 查询所有用户
     */
    List<User> selectAllUsers();
    /**
     * 搜索用户
     */
    List<User> searchUsers(@Param("username") String username, @Param("email") String email, @Param("status") Integer status);
    /**
     * 获取用户数量
     */
    int countByUsername(String username);
    /**
     * 获取用户数量
     */
    int countByEmail(String email);
    /**
     * 删除用户
     */
    int deleteUser(Integer userId);
    /**
     * 获取用户数量
     */
    int selectUserCountByMonth(@Param("year") int year, @Param("month") int month);
    /**
     * 获取用户数量
     */
    Integer selectTotalDurationByUserAndDate(@Param("userId") Integer userId,
                                          @Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
    /**
     * 获取用户行为类型分布
     */
    List<Map<String, Object>> selectBehaviorTypeDistribution(@Param("userId") Integer userId,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);
    
    /**
     * 获取用户行为记录数量
     */
    Integer countBehaviorRecordsByTypeAndDate(@Param("userId") Integer userId,
                                             @Param("typeName") String typeName,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);
    /**
     * 获取用户运动日期
     */
    List<LocalDate> selectExerciseDatesByUser(@Param("userId") Integer userId);
}