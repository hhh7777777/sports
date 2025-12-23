package com.hongyuting.sports.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 行为记录实体类
 */
@Setter
@Getter
@Data
public class Behavior {
    private Long recordId;
    private Integer userId;
    private Integer typeId;
    private LocalDate recordDate;
    private Integer duration;
    private String content;
    private String imageUrl;
    private Double distance; // 距离（公里）
    private Integer calories; // 消耗卡路里
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String typeName;

}