package com.hongyuting.sports.service;

import com.hongyuting.sports.dto.BadgeDTO;
import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.Badge;

import java.util.List;

/**
 * 徽章服务接口
 */
public interface BadgeService {

    /**
     * 获取所有徽章
     */
    List<Badge> getAllBadges();

    /**
     * 添加徽章
     */
    ResponseDTO addBadge(Badge badge);

    /**
     * 更新徽章
     */
    ResponseDTO updateBadge(Badge badge);

    /**
     * 删除徽章
     */
    ResponseDTO deleteBadge(Integer badgeId);

    /**
     * 获取徽章详情
     */
    Badge getBadgeById(Integer badgeId);

    /**
     * 根据类型获取徽章
     */
    List<Badge> getBadgesByType(String badgeType);

    /**
     * 根据名称获取徽章
     */
    List<Badge> getBadgesByName(String name);

    /**
     * 添加徽章（DTO版本）
     */
    ResponseDTO addBadge(BadgeDTO badgeDTO);

    /**
     * 更新徽章（DTO版本）
     */
    ResponseDTO updateBadge(BadgeDTO badgeDTO);

    /**
     * 获取徽章总数
     */
    Integer getTotalBadgeCount();

    /**
     * 获取用户成就
     */
    List<Badge> getUserAchievements(Integer userId);

    /**
     * 获取用户总积分
     */
    Integer getUserTotalPoints(Integer userId);

    /**
     * 获取用户最近获得的徽章
     */
    List<Badge> getRecentlyAchievedBadges(Integer userId, Integer limit);

    /**
     * 检查用户是否拥有指定徽章
     */
    boolean checkUserHasBadge(Integer userId, Integer badgeId);

    /**
     * 根据条件类型获取徽章
     */
    List<Badge> getBadgesByConditionType(String conditionType);

    /**
     * 根据等级获取徽章
     */
    List<Badge> getBadgesByLevel(Integer level);

    /**
     * 授予用户徽章
     */
    ResponseDTO grantBadgeToUser(Integer userId, Integer badgeId);

    /**
     * 更新用户成就进度
     */
    ResponseDTO updateUserAchievementProgress(Integer userId, Integer badgeId, Integer progress);

    /**
     * 根据用户行为自动授予徽章
     */
    ResponseDTO autoGrantBadgesBasedOnBehavior(Integer userId);
}