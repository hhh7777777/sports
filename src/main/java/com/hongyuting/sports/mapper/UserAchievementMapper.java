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

    List<UserBadge> selectRecentlyAchievedBadges(@Param("userId") Integer userId,
                                                 @Param("limit") Integer limit);

    Integer countUsersWithBadge(@Param("badgeId") Integer badgeId);

    Integer countAchievementsByUserAndDate(@Param("userId") Integer userId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    int insertUserAchievement(UserAchievement userAchievement);

    int updateUserAchievement(UserBadge userAchievement);

    UserAchievement selectUserAchievement(@Param("userId") int userId, @Param("badgeId") int badgeId);

    boolean existsByUserIdAndBadgeId(@Param("userId") int userId, @Param("badgeId") int badgeId);

    List<UserAchievement> selectByBadgeId(@Param("badgeId") int badgeId);

    List<UserAchievement> selectRecentlyByUserId(@Param("userId") int userId);

    int countAchievementsByUserId(@Param("userId") int userId);

    int sumPointsByUserId(@Param("userId") int userId);


    List<UserBadge> selectUserBadgesByUserId(@Param("userId") Integer userId);

    int insertUserBadge(UserBadge userBadge);

    UserBadge selectUserBadge(@Param("userId") Integer userId, @Param("badgeId") Integer badgeId);

}