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
    List<Behavior> selectAllBehaviorRecords();
    Behavior selectBehaviorRecordById(Long recordId);
    List<Behavior> selectBehaviorRecordsByUserId(Integer userId);
    List<Behavior> selectBehaviorRecordsByUserAndDate(@Param("userId") Integer userId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);
    int insertBehaviorRecord(Behavior behavior);
    int updateBehaviorRecord(Behavior behavior);
    int deleteBehaviorRecord(Long recordId);
    Integer selectTotalDurationByUserAndDate(@Param("userId") Integer userId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);
    List<Map<String, Object>> selectBehaviorTypeDistribution(@Param("userId") Integer userId,
                                                             @Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);
    
    // 新增方法
    int selectTotalBehaviorRecords();
    int selectBehaviorCountByTypeAndDate(@Param("typeId") Integer typeId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);
    List<Behavior> selectRecentBehaviors(@Param("limit") int limit);
    
    // BehaviorType related methods
    List<BehaviorType> selectAllBehaviorTypes();
    BehaviorType selectBehaviorTypeById(@Param("typeId") Integer typeId);
    int insertBehaviorType(BehaviorType behaviorType);
    int updateBehaviorType(BehaviorType behaviorType);
    int deleteBehaviorType(@Param("typeId") Integer typeId);
}