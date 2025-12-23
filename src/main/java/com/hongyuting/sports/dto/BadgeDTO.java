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
    private String badgeType; // 徽章类型：activity(活动徽章)、achievement(成就徽章)、participation(参与徽章)
    private Boolean achieved; // 是否已获得
    private Integer progress; // 进度百分比(0-100)
}