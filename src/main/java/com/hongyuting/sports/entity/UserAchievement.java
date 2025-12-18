package com.hongyuting.sports.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class UserAchievement {

    @Getter
    @Setter
    private Integer id;
    private Integer userId;
    private Integer badgeId;
    private LocalDateTime achieveTime;
    private Integer progress;

}