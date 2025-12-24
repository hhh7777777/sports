package com.hongyuting.sports.mapper;

import com.hongyuting.sports.entity.UserAchievement;
import com.hongyuting.sports.entity.UserBadge;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户成就映射接口
 */
public interface UserAchievementMapper {
    /**
     * 根据用户ID查询最近获得徽章
     */
    List<UserBadge> selectRecentlyAchievedBadges(@Param("userId") Integer userId,
                                                 @Param("limit") Integer limit);
    /**
     * 根据用户ID和日期查询用户成就数量
     */
    Integer countAchievementsByUserAndDate(@Param("userId") Integer userId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
    /**
     * 添加用户成就
     */
    int insertUserAchievement(UserAchievement userAchievement);
    /**
     * 更新用户成就
     */
    int updateUserAchievement(UserBadge userAchievement);
    /**
     * 根据用户ID和徽章ID查询用户成就
     */
    UserAchievement selectUserAchievement(@Param("userId") int userId, @Param("badgeId") int badgeId);
    /**
     * 判断用户是否已获得指定徽章
     */
    boolean existsByUserIdAndBadgeId(@Param("userId") int userId, @Param("badgeId") int badgeId);
    /**
     * 根据徽章ID查询用户成就
     */
    List<UserAchievement> selectByBadgeId(@Param("badgeId") int badgeId);
    /**
     * 根据用户ID查询最近获得徽章
     */
    List<UserAchievement> selectRecentlyByUserId(@Param("userId") int userId);
    /**
     * 根据用户ID查询用户成就数量
     */
    int countAchievementsByUserId(@Param("userId") int userId);
    /**
     * 根据用户ID查询用户总积分
     */
    int sumPointsByUserId(@Param("userId") int userId);
    /**
     * 根据用户ID查询用户成就
     */
    List<UserAchievement> selectByUserId(@Param("userId") Integer userId);
    /**
     * 根据用户ID查询用户徽章
     */
    List<UserBadge> selectUserBadgesByUserId(@Param("userId") Integer userId);
    /**
     * 添加用户徽章
     */
    int insertUserBadge(UserBadge userBadge);
    /**
      * 根据用户ID和徽章ID查询用户徽章
      */
    UserBadge selectUserBadge(@Param("userId") Integer userId, @Param("badgeId") Integer badgeId);

}