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
    
    // 添加缺失的字段
    private String icon;
    private String color;
    private boolean obtained;
    
    // 添加缺失的getter和setter方法
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public boolean isObtained() {
        return obtained;
    }
    
    public void setObtained(boolean obtained) {
        this.obtained = obtained;
    }
}