package com.hongyuting.sports.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 行为记录传输对象
 */
@Data
public class BehaviorDTO {
    private Integer userId;
    private Integer typeId;
    private LocalDate recordDate;
    private Integer duration;
    private String content;
    private String imageUrl;
    private Double distance; // 距离（公里）
    private Integer calories; // 消耗卡路里
}