package com.hongyuting.sports.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户成就实体类
 */
@Data
public class UserAchievement {
    private Integer id;
    private Integer userId;
    private Integer badgeId;
    private LocalDateTime achieveTime;//成就时间
    private Integer progress;//进度

}