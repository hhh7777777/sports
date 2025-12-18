package com.hongyuting.sports.entity;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBadge {

    private Long id;
    private Integer userId;
    private Integer badgeId;
    private LocalDateTime achieveTime;
    private Integer progress = 100;
    @Setter
    @Getter
    private LocalDateTime updateTime;

}