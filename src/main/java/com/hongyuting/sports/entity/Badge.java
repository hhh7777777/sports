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
    private String icon;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    public String getColor() {
        // 根据徽章等级返回对应颜色
        if (level == null) {
            return "#808080"; // 默认灰色
        }
        
        switch (level) {
            case 1:
                return "#808080"; // 灰色 - 普通
            case 2:
                return "#C0C0C0"; // 银色 - 不常见
            case 3:
                return "#FFD700"; // 金色 - 稀有
            case 4:
                return "#9400D3"; // 紫色 - 史贵
            case 5:
                return "#FF4500"; // 橙红色 - 传说
            default:
                return "#808080"; // 默认灰色
        }
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
}