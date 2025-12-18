package com.hongyuting.sports.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Data
public class Badge {
    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private Integer points;

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
    @Setter
    private LocalDateTime updateTime;

}