package com.hongyuting.sports.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminLog {
    private Long logId;
    private Integer adminId;
    private String operation;
    private String targetType;
    private Integer targetId;
    private String detail;
    private String ipAddress;
    private LocalDateTime operationTime;
}