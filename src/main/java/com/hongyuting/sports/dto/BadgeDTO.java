package com.hongyuting.sports.dto;

import lombok.Data;

@Data
public class BadgeDTO {
    private Integer badgeId;
    private String badgeName;
    private String description;
    private String iconUrl;
    private String conditionType;
    private Integer conditionValue;
    private Integer level;
    private Integer rewardPoints;
}