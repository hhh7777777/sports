package com.hongyuting.sports.mapper;

import com.hongyuting.sports.entity.AchievementBadge;
import com.hongyuting.sports.entity.Badge;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AchievementBadgeMapper {
    int insertBadge(Badge badge);
    int updateBadge(Badge badge);
    int disableBadge(@Param("badgeId") int badgeId);
    Badge selectBadgeById(@Param("badgeId") int badgeId);
    List<Badge> selectAllBadges();
    List<Badge> selectBadgesByConditionType(@Param("conditionType") String conditionType);
    List<Badge> selectBadgesByLevel(@Param("level") int level);
    int deleteBadge(Integer badgeId);
    Integer selectTotalBadgeCount();
}