package com.hongyuting.sports.dto;

import lombok.Data;
/**
 * 徽章传输对象
 */
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
    private Integer status;
}