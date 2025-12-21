package com.hongyuting.sports.service;

import com.hongyuting.sports.dto.BehaviorDTO;
import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.Behavior;
import com.hongyuting.sports.entity.BehaviorType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 行为服务接口
 */
public interface BehaviorService {
    ResponseDTO addBehaviorRecord(BehaviorDTO behaviorDTO);
    ResponseDTO updateBehaviorRecord(Behavior record);
    ResponseDTO deleteBehaviorRecord(Long recordId);
    Behavior getBehaviorRecordById(Long recordId);
    List<Behavior> getBehaviorRecordsByUser(Integer userId);
    List<Behavior> getBehaviorRecordsByUserAndDate(Integer userId, LocalDate startDate, LocalDate endDate);
    List<Behavior> getTodayBehaviorRecords(Integer userId);
    Integer getTotalBehaviorDuration(Integer userId, LocalDate startDate, LocalDate endDate);
    List<BehaviorType> getAllBehaviorTypes();
    ResponseDTO addBehaviorType(BehaviorType behaviorType);
    ResponseDTO updateBehaviorType(BehaviorType behaviorType);
    ResponseDTO deleteBehaviorType(Integer typeId);
    BehaviorType getBehaviorTypeById(Integer typeId);

    // 新增方法：获取行为类型分布
    List<Map<String, Object>> getBehaviorTypeDistribution(Integer userId, LocalDate startDate, LocalDate endDate);

    // 新增方法：获取行为记录总数
    Integer getTotalBehaviorRecords();
}