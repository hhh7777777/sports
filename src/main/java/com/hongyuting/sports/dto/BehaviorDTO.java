package com.hongyuting.sports.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BehaviorDTO {
    private Integer userId;
    private Integer typeId;
    private LocalDate recordDate;
    private Integer duration;
    private String content;
    private String imageUrl;
}