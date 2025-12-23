package com.hongyuting.sports.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 活动DTO类
 */
@Setter
@Getter
public class ActivityDTO {

    private String id;
    private String name;
    private String description;
    private String type;
    private Integer currentProgress;
    private Integer requiredProgress;
    private String icon;
    private String color;
    private String difficulty;

    public ActivityDTO(String id, String name, String description, String type,
                       Integer currentProgress, Integer requiredProgress, String icon,
                       String color, String difficulty) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.currentProgress = currentProgress;
        this.requiredProgress = requiredProgress;
        this.icon = icon;
        this.color = color;
        this.difficulty = difficulty;
    }

}