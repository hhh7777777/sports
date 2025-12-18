package com.hongyuting.sports.mapper;

import com.hongyuting.sports.entity.Behavior;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface BehaviorRecordMapper {
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
}