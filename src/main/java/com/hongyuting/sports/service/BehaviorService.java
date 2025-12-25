package com.hongyuting.sports.service;

import com.hongyuting.sports.dto.BehaviorDTO;
import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.Behavior;
import com.hongyuting.sports.entity.BehaviorType;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 行为记录服务接口
 */
public interface BehaviorService {
    /**
     * 添加行为记录
     */
    ResponseDTO addBehaviorRecord(BehaviorDTO behaviorDTO);

    /**
     * 修改行为记录
     */
    ResponseDTO updateBehaviorRecord(Behavior record);

    /**
     * 删除行为记录
     */
    ResponseDTO deleteBehaviorRecord(Long recordId);

    /**
     * 根据ID获取行为记录
     */
    Behavior getBehaviorRecordById(Long recordId);

    /**
     * 根据用户ID获取行为记录
     */
    List<Behavior> getBehaviorRecordsByUser(Integer userId);

    /**
     * 根据用户ID和日期范围获取行为记录
     */
    List<Behavior> getBehaviorRecordsByUserAndDate(Integer userId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取用户今日行为记录
     */
    List<Behavior> getTodayBehaviorRecords(Integer userId);

    /**
     * 获取用户行为总时长
     */
    Integer getTotalBehaviorDuration(Integer userId, LocalDate startDate, LocalDate endDate);

    /**
     * 根据日期范围获取行为记录
     */
    List<Behavior> getBehaviorRecordsByDate(LocalDate startDate, LocalDate endDate);

    /**
     * 获取所有行为类型
     */
    List<BehaviorType> getAllBehaviorTypes();

    /**
     * 根据ID获取行为类型
     */
    BehaviorType getBehaviorTypeById(Integer typeId);

    /**
     * 添加行为类型
     */
    ResponseDTO addBehaviorType(BehaviorType behaviorType);

    /**
     * 修改行为类型
     */
    ResponseDTO updateBehaviorType(BehaviorType behaviorType);

    /**
     * 删除行为类型
     */
    ResponseDTO deleteBehaviorType(Integer typeId);

    /**
     * 获取行为记录总数
     */
    int getTotalBehaviorRecords();

    /**
     * 根据类型和日期范围获取行为记录数量
     */
    int getBehaviorCountByTypeAndDate(Integer typeId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取最新的行为记录
     */
    List<Behavior> getRecentBehaviors(int limit);

    List<Map<String, Object>> getBehaviorTypeDistribution(Integer userId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取周统计
     */
    Map<String, Object> getWeeklyStatistics(Integer userId);

    /**
     * 获取月统计
     */
    Map<String, Object> getMonthlyStatistics(Integer userId);

    /**
     * 获取所有行为记录
     */
    List<Behavior> getAllBehaviors();
    
    /**
     * 根据类型ID获取行为记录
     */
    List<Behavior> getBehaviorRecordsByType(Integer typeId);
    
    /**
     * 根据用户ID和类型ID获取行为记录
     */
    List<Behavior> getBehaviorRecordsByUserAndType(Integer userId, Integer typeId);
    
    /**
     * 根据类型ID和日期范围获取行为记录
     */
    List<Behavior> getBehaviorRecordsByTypeAndDate(Integer typeId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 根据用户ID、类型ID和日期范围获取行为记录
     */
    List<Behavior> getBehaviorRecordsByUserTypeAndDate(Integer userId, Integer typeId, LocalDate startDate, LocalDate endDate);
}