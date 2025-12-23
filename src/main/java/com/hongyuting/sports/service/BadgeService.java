package com.hongyuting.sports.service;

import com.hongyuting.sports.entity.Badge;
import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.UserBadge;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 徽章服务接口
 */
public interface BadgeService {

    ResponseDTO addBadge(Badge badge);

    ResponseDTO updateBadge(Badge badge);

    ResponseDTO deleteBadge(Integer badgeId);

    List<Badge> getAllBadges();

    Badge getBadgeById(Integer badgeId);

    List<Badge> getBadgesByLevel(Integer level);

    List<Badge> getBadgesByConditionType(String conditionType);

    List<UserBadge> getUserAchievements(Integer userId);

    ResponseDTO grantBadgeToUser(Integer userId, Integer badgeId);

    ResponseDTO updateUserAchievementProgress(Integer userId, Integer badgeId, Integer progress);

    boolean checkUserHasBadge(Integer userId, Integer badgeId);

    List<UserBadge> getRecentlyAchievedBadges(Integer userId, Integer limit);

    Integer getUserTotalPoints(Integer userId);

    List<UserBadge> getUserBadges(Integer userId);

    @Transactional
    ResponseDTO assignBadgeToUser(Integer userId, Integer badgeId);

    @Transactional
    ResponseDTO updateBadgeProgress(UserBadge userBadge);

    // 新增方法：获取徽章总数
    Integer getTotalBadgeCount();
    
    // 新增方法：根据用户行为自动授予徽章
    ResponseDTO autoGrantBadgesBasedOnBehavior(Integer userId);
    
    // 根据徽章类型获取徽章
    List<Badge> getBadgesByType(String badgeType);
    
    // 获取圣诞限定徽章
    List<Badge> getChristmasBadges();
}