package com.hongyuting.sports.mapper;

import com.hongyuting.sports.entity.Behavior;
import com.hongyuting.sports.entity.BehaviorType;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 行为记录映射接口
 */
public interface BehaviorMapper {
    /**
     * 查询所有行为记录
     */
    List<Behavior> selectAllBehaviorRecords();
    /**
     * 根据ID查询行为记录
     */
    Behavior selectBehaviorRecordById(Long recordId);
    /**
     * 根据用户ID查询行为记录
     */
    List<Behavior> selectBehaviorRecordsByUserId(Integer userId);
    /**
     * 根据用户ID和日期查询行为记录
     */
    List<Behavior> selectBehaviorRecordsByUserAndDate(@Param("userId") Integer userId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);
    /**
     * 根据日期查询行为记录
     */
    List<Behavior> selectBehaviorRecordsByDate(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
    /**
     * 添加行为记录
     */
    int insertBehaviorRecord(Behavior behavior);
    /**
     * 更新行为记录
     */
    int updateBehaviorRecord(Behavior behavior);
    /**
     * 删除行为记录
     */
    int deleteBehaviorRecord(Long recordId);
    /**
     * 查询用户指定日期内行为总时长
     */
    Integer selectTotalDurationByUserAndDate(@Param("userId") Integer userId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);
    /**
     * 查询用户指定日期内行为类型分布
     */
    List<Map<String, Object>> selectBehaviorTypeDistribution(@Param("userId") Integer userId,
                                                             @Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);
    
    /**
     * 获取行为记录总数
     */
    int selectTotalBehaviorRecords();
    /**
     * 根据行为类型和日期查询行为数量
     */
    int selectBehaviorCountByTypeAndDate(@Param("typeId") Integer typeId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);
    /**
     * 查询最近行为记录
     */
    List<Behavior> selectRecentBehaviors(@Param("limit") int limit);
    
    /**
     * 查询所有行为类型
     */
    List<BehaviorType> selectAllBehaviorTypes();
    /**
     * 根据ID查询行为类型
     */
    BehaviorType selectBehaviorTypeById(@Param("typeId") Integer typeId);
    /**
     * 添加行为类型
     */
    int insertBehaviorType(BehaviorType behaviorType);
    /**
     * 更新行为类型
     */
    int updateBehaviorType(BehaviorType behaviorType);
    /**
     * 删除行为类型
     */
    int deleteBehaviorType(@Param("typeId") Integer typeId);
    
    /**
     * 根据类型ID查询行为记录
     */
    List<Behavior> selectBehaviorRecordsByTypeId(@Param("typeId") Integer typeId);
    
    /**
     * 根据用户ID和类型ID查询行为记录
     */
    List<Behavior> selectBehaviorRecordsByUserAndTypeId(@Param("userId") Integer userId, @Param("typeId") Integer typeId);
    
    /**
     * 根据类型ID和日期查询行为记录
     */
    List<Behavior> selectBehaviorRecordsByTypeAndDate(@Param("typeId") Integer typeId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);
    
    /**
     * 根据用户ID、类型ID和日期查询行为记录
     */
    List<Behavior> selectBehaviorRecordsByUserTypeAndDate(@Param("userId") Integer userId,
                                                         @Param("typeId") Integer typeId,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);
}