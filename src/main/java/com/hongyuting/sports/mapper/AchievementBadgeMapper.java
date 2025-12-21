package com.hongyuting.sports.mapper;

import com.hongyuting.sports.entity.Badge;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 徽章映射接口
 */
public interface AchievementBadgeMapper {
    /**
     * 添加徽章
     */
    int insertBadge(Badge badge);
    /**
     * 更新徽章
     */
    int updateBadge(Badge badge);
    
    /**
     * 禁用徽章
     */
    int disableBadge(int badgeId);

    /**
     * 根据ID查询徽章
     */
    Badge selectBadgeById(@Param("badgeId") int badgeId);
    /**
     * 查询所有徽章
     */
    List<Badge> selectAllBadges();
    /**
     * 根据条件查询徽章
     */
    List<Badge> selectBadgesByConditionType(@Param("conditionType") String conditionType);
    /**
     * 根据等级查询徽章
     */
    List<Badge> selectBadgesByLevel(@Param("level") int level);
    /**
     * 根据类型查询徽章
     */
    List<Badge> selectBadgesByType(@Param("badgeType") String badgeType);
    /**
     * 删除徽章
     */
    int deleteBadge(Integer badgeId);
    /**
     * 获取徽章总数
     */
    Integer selectTotalBadgeCount();
}