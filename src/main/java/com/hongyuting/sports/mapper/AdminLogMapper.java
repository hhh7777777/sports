package com.hongyuting.sports.mapper;

import com.hongyuting.sports.entity.AdminLog;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员日志映射接口
 */
public interface AdminLogMapper extends BaseMapper<AdminLog, Long> {

    /**
     * 根据管理员ID查询日志
     */
    List<AdminLog> selectByAdminId(@Param("adminId") Integer adminId);

    /**
     * 根据操作类型查询日志
     */
    List<AdminLog> selectByOperation(@Param("operation") String operation);

    /**
     * 根据条件查询日志
     */
    List<AdminLog> selectByCondition(@Param("adminId") Integer adminId,
                                     @Param("operation") String operation,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 根据目标查询日志
     */
    List<AdminLog> selectByTarget(@Param("targetType") String targetType,
                                  @Param("targetId") Integer targetId);

    /**
     * 统计操作次数
     */
    Integer selectOperationCount(@Param("adminId") Integer adminId,
                                 @Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 删除指定时间前的日志
     */
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);
    
    /**
     * 分页查询所有日志
     */
    List<AdminLog> selectAllWithPaging(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 统计符合条件的日志数量
     */
    Integer selectCountByCondition(@Param("adminId") Integer adminId,
                                  @Param("operation") String operation,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据条件分页查询日志
     */
    List<AdminLog> selectByConditionWithPaging(@Param("adminId") Integer adminId,
                                              @Param("operation") String operation,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime,
                                              @Param("offset") int offset,
                                              @Param("limit") int limit);
}