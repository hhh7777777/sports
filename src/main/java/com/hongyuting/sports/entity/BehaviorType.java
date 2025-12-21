package com.hongyuting.sports.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 行为类型实体类
 */
@Data
public class BehaviorType {
    @Setter
    @Getter
    private Integer typeId;
    private String typeName;
    private String description;
    private String iconUrl;
    private Integer status = 1;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}