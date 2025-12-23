package com.hongyuting.sports.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 徽章实体类
 */
@Data
public class Badge {
    private Integer badgeId;
    private String badgeName;
    private String description;
    private String iconUrl;
    private String conditionType;
    private Integer conditionValue;
    private Integer level;
    private Integer rewardPoints;
    private Integer status;
    private String badgeType;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}