package com.hongyuting.sports.mapper;

import com.hongyuting.sports.entity.User;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 用户映射接口
 */
public interface UserMapper extends BaseMapper<User, Integer> {

    /**
     * 根据用户名查询用户
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 搜索用户
     */
    List<User> searchUsers(@Param("username") String username,
                           @Param("email") String email,
                           @Param("status") Integer status);

    /**
     * 检查用户名是否存在
     */
    int countByUsername(@Param("username") String username);

    /**
     * 检查邮箱是否存在
     */
    int countByEmail(@Param("email") String email);

    /**
     * 更新用户头像
     */
    int updateUserAvatar(@Param("userId") Integer userId,
                         @Param("avatar") String avatar);

    /**
     * 按月统计用户数量
     */
    int selectUserCountByMonth(@Param("year") int year,
                               @Param("month") int month);

    /**
     * 查询用户行为总时长
     */
    Integer selectTotalDurationByUserAndDate(@Param("userId") Integer userId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    /**
     * 查询用户行为类型分布
     */
    List<Map<String, Object>> selectBehaviorTypeDistribution(@Param("userId") Integer userId,
                                                             @Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);

    /**
     * 统计用户行为记录数量
     */
    Integer countBehaviorRecordsByTypeAndDate(@Param("userId") Integer userId,
                                              @Param("typeName") String typeName,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    /**
     * 查询用户运动日期
     */
    List<LocalDate> selectExerciseDatesByUser(@Param("userId") Integer userId);
    
    /**
     * 查询用户总运动时长
     */
    Integer selectTotalDurationByUser(@Param("userId") Integer userId);
    
    /**
     * 统计用户行为记录数量
     */
    int selectCountByUserId(@Param("userId") Integer userId);
    
    /**
     * 获取指定月份数量的用户增长数据
     */
    List<Map<String, Object>> selectUserGrowthStats(@Param("months") int months);
}