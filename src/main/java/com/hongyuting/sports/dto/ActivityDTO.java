package com.hongyuting.sports.dto;

/**
 * 活动DTO类
 */
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

    public ActivityDTO() {
    }

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

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(Integer currentProgress) {
        this.currentProgress = currentProgress;
    }

    public Integer getRequiredProgress() {
        return requiredProgress;
    }

    public void setRequiredProgress(Integer requiredProgress) {
        this.requiredProgress = requiredProgress;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}