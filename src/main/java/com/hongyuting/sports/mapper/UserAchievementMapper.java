package com.hongyuting.sports.mapper;

import com.hongyuting.sports.entity.UserAchievement;
import com.hongyuting.sports.entity.UserBadge;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;

/**
 * 用户成就映射接口
 */
public interface UserAchievementMapper extends BaseMapper<UserAchievement, Integer> {

    /**
     * 根据用户ID查询用户成就
     */
    List<UserAchievement> selectByUserId(@Param("userId") Integer userId);

    /**
     * 查询最近获得的徽章
     */
    List<UserBadge> selectRecentlyAchievedBadges(@Param("userId") Integer userId,
                                                 @Param("limit") Integer limit);

    /**
     * 统计拥有指定徽章的用户数量
     */
    Integer countUsersWithBadge(@Param("badgeId") Integer badgeId);

    /**
     * 按日期统计用户成就数量
     */
    Integer countAchievementsByUserAndDate(@Param("userId") Integer userId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /**
     * 检查用户是否拥有徽章
     */
    boolean existsByUserIdAndBadgeId(@Param("userId") Integer userId,
                                     @Param("badgeId") Integer badgeId);

    /**
     * 根据徽章ID查询用户成就
     */
    List<UserAchievement> selectByBadgeId(@Param("badgeId") Integer badgeId);

    /**
     * 查询用户最近获得的成就
     */
    List<UserAchievement> selectRecentlyByUserId(@Param("userId") Integer userId);

    /**
     * 统计用户成就数量
     */
    Integer countAchievementsByUserId(@Param("userId") Integer userId);

    /**
     * 计算用户总积分
     */
    Integer sumPointsByUserId(@Param("userId") Integer userId);

    /**
     * 查询用户徽章
     */
    List<UserBadge> selectUserBadgesByUserId(@Param("userId") Integer userId);

    /**
     * 插入用户徽章
     */
    int insertUserBadge(UserBadge userBadge);

    /**
     * 查询用户徽章
     */
    UserBadge selectUserBadge(@Param("userId") Integer userId,
                              @Param("badgeId") Integer badgeId);
}