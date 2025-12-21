package com.hongyuting.sports.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户徽章实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBadge {
    private Long id;
    private Integer userId;
    private Integer badgeId;
    private LocalDateTime achieveTime;//获得时间
    private Integer progress = 100;
    private LocalDateTime updateTime;
}