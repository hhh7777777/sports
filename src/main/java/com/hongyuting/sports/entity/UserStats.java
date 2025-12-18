package com.hongyuting.sports.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserStats {
    private Integer userId;
    private Integer totalBehaviorRecords;
    private Integer totalBehaviorDuration; // 分钟
    private Integer achievedBadges;
    private Integer totalPoints;
    private LocalDateTime lastActivityTime;

    public UserStats() {
        this.totalBehaviorRecords = 0;
        this.totalBehaviorDuration = 0;
        this.achievedBadges = 0;
        this.totalPoints = 0;
        this.lastActivityTime = LocalDateTime.now();
    }

    public void setTotalBehaviors(int totalBehaviorRecords) {
        this.totalBehaviorRecords = totalBehaviorRecords;
    }

    public void setAchievementCount(int achievedBadges) {
        this.achievedBadges = achievedBadges;
    }

    public void setTotalDuration(int totalBehaviorDuration) {
        this.totalBehaviorDuration = totalBehaviorDuration;
    }

    public Integer getTotalDuration() {
        return this.totalBehaviorDuration;
    }

    // 为了保持一致性，可以添加其他 setter 方法
    public void setTotalRecords(int totalRecords) {
        this.totalBehaviorRecords = totalRecords;
    }

    public void setConsecutiveDays(int consecutiveDays) {
        // 如果需要这个字段，可以添加到类中
    }
}