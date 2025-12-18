package com.hongyuting.sports.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AchievementBadge {
    private Integer badgeId;
    private String badgeName;
    private String description;
    private String iconUrl;
    private String conditionType;
    private Integer conditionValue;
    private Integer level;
    private Integer rewardPoints;
    private Integer status;
    private LocalDateTime createTime;
}