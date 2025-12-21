package com.hongyuting.sports.entity;

import lombok.Data;
import java.time.LocalDateTime;
/**
 * 管理员操作日志实体类
 */
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