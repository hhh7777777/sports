package com.hongyuting.sports.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BehaviorType {
    private Integer typeId;
    private String typeName;
    private String description;
    private String iconUrl;
    private Integer status;
    private LocalDateTime createTime;
}