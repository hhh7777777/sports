package com.hongyuting.sports.service.impl;

import com.hongyuting.sports.dto.BehaviorDTO;
import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.Behavior;
import com.hongyuting.sports.entity.BehaviorType;
import com.hongyuting.sports.mapper.BehaviorMapper;
import com.hongyuting.sports.service.BehaviorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * 行为记录服务实现类
 */
public class BehaviorServiceImpl implements BehaviorService {

    private final BehaviorMapper behaviorMapper;

    @Override
    @Transactional
    public ResponseDTO addBehaviorRecord(BehaviorDTO behaviorDTO) {
        try {
            // 参数校验
            if (behaviorDTO == null) {
                return ResponseDTO.error("行为记录信息不能为空");
            }
            
            if (behaviorDTO.getUserId() == null) {
                return ResponseDTO.error("用户ID不能为空");
            }
            
            if (behaviorDTO.getTypeId() == null) {
                return ResponseDTO.error("类型ID不能为空");
            }
            
            if (behaviorDTO.getDuration() == null || behaviorDTO.getDuration() <= 0) {
                return ResponseDTO.error("时长必须大于0");
            }

            Behavior record = new Behavior();
            record.setUserId(behaviorDTO.getUserId());
            record.setTypeId(behaviorDTO.getTypeId());
            record.setRecordDate(behaviorDTO.getRecordDate() != null ? behaviorDTO.getRecordDate() : LocalDate.now());
            record.setDuration(behaviorDTO.getDuration());
            record.setContent(behaviorDTO.getContent());
            record.setImageUrl(behaviorDTO.getImageUrl());
            record.setDistance(behaviorDTO.getDistance());
            record.setCalories(behaviorDTO.getCalories());
            record.setCreateTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());

            int result = behaviorMapper.insertBehaviorRecord(record);
            if (result > 0) {
                log.info("行为记录添加成功：记录ID={}", record.getRecordId());
                return ResponseDTO.success("行为记录添加成功", record.getRecordId());
            } else {
                log.warn("行为记录添加失败：用户ID={}，类型ID={}", behaviorDTO.getUserId(), behaviorDTO.getTypeId());
                return ResponseDTO.error("行为记录添加失败");
            }
        } catch (Exception e) {
            log.error("行为记录添加异常: ", e);
            return ResponseDTO.error("行为记录添加异常: " + e.getMessage());
        }
    }

    @Override
    public Behavior getBehaviorRecordById(Long recordId) {
        try {
            if (recordId == null) {
                return null;
            }
            return behaviorMapper.selectBehaviorRecordById(recordId);
        } catch (Exception e) {
            log.error("根据ID获取行为记录异常: recordId={}", recordId, e);
            return null;
        }
    }

    @Override
    public List<Behavior> getBehaviorRecordsByUser(Integer userId) {
        try {
            if (userId == null) {
                return List.of();
            }
            return behaviorMapper.selectBehaviorRecordsByUserId(userId);
        } catch (Exception e) {
            log.error("根据用户ID获取行为记录异常: userId={}", userId, e);
            return List.of();
        }
    }

    @Override
    public List<Behavior> getBehaviorRecordsByUserAndDate(Integer userId, LocalDate startDate, LocalDate endDate) {
        try {
            if (userId == null || startDate == null || endDate == null) {
                return List.of();
            }
            return behaviorMapper.selectBehaviorRecordsByUserAndDate(userId, startDate, endDate);
        } catch (Exception e) {
            log.error("根据用户ID和日期范围获取行为记录异常: userId={}, startDate={}, endDate={}", userId, startDate, endDate, e);
            return List.of();
        }
    }

    @Override
    public List<Behavior> getTodayBehaviorRecords(Integer userId) {
        try {
            if (userId == null) {
                return List.of();
            }
            LocalDate today = LocalDate.now();
            return getBehaviorRecordsByUserAndDate(userId, today, today);
        } catch (Exception e) {
            log.error("获取今日行为记录异常: userId={}", userId, e);
            return List.of();
        }
    }

    @Override
    public Integer getTotalBehaviorDuration(Integer userId, LocalDate startDate, LocalDate endDate) {
        try {
            if (userId == null || startDate == null || endDate == null) {
                return 0;
            }
            Integer duration = behaviorMapper.selectTotalDurationByUserAndDate(userId, startDate, endDate);
            return duration != null ? duration : 0;
        } catch (Exception e) {
            log.error("获取行为总时长异常: userId={}, startDate={}, endDate={}", userId, startDate, endDate, e);
            return 0;
        }
    }
    
    @Override
    public List<Behavior> getBehaviorRecordsByDate(LocalDate startDate, LocalDate endDate) {
        try {
            if (startDate == null || endDate == null) {
                return List.of();
            }
            return behaviorMapper.selectBehaviorRecordsByDate(startDate, endDate);
        } catch (Exception e) {
            log.error("根据日期范围获取行为记录异常: startDate={}, endDate={}", startDate, endDate, e);
            return List.of();
        }
    }
    
    @Override
    public int getTotalBehaviorRecords() {
        try {
            return behaviorMapper.selectTotalBehaviorRecords();
        } catch (Exception e) {
            log.error("获取行为记录总数异常", e);
            return 0;
        }
    }
    
    @Override
    public int getBehaviorCountByTypeAndDate(Integer typeId, LocalDate startDate, LocalDate endDate) {
        try {
            if (typeId == null || startDate == null || endDate == null) {
                return 0;
            }
            return behaviorMapper.selectBehaviorCountByTypeAndDate(typeId, startDate, endDate);
        } catch (Exception e) {
            log.error("获取行为记录数量异常: typeId={}, startDate={}, endDate={}", typeId, startDate, endDate, e);
            return 0;
        }
    }
    
    @Override
    public List<Behavior> getRecentBehaviors(int limit) {
        try {
            return behaviorMapper.selectRecentBehaviors(limit);
        } catch (Exception e) {
            log.error("获取最新行为记录异常: limit={}", limit, e);
            return List.of();
        }
    }

    @Override
    public List<BehaviorType> getAllBehaviorTypes() {
        try {
            return behaviorMapper.selectAllBehaviorTypes();
        } catch (Exception e) {
            log.error("获取所有行为类型异常", e);
            return List.of();
        }
    }

    @Override
    public BehaviorType getBehaviorTypeById(Integer typeId) {
        try {
            if (typeId == null) {
                return null;
            }
            return behaviorMapper.selectBehaviorTypeById(typeId);
        } catch (Exception e) {
            log.error("根据ID获取行为类型异常: typeId={}", typeId, e);
            return null;
        }
    }

    @Override
    @Transactional
    public ResponseDTO addBehaviorType(BehaviorType behaviorType) {
        try {
            // 参数校验
            if (behaviorType == null) {
                return ResponseDTO.error("行为类型信息不能为空");
            }
            
            if (!StringUtils.hasText(behaviorType.getTypeName())) {
                return ResponseDTO.error("行为类型名称不能为空");
            }

            behaviorType.setStatus(1);
            behaviorType.setCreateTime(LocalDateTime.now());
            int result = behaviorMapper.insertBehaviorType(behaviorType);
            if (result > 0) {
                log.info("行为类型添加成功：类型ID={}", behaviorType.getTypeId());
                return ResponseDTO.success("行为类型添加成功", behaviorType.getTypeId());
            } else {
                log.warn("行为类型添加失败：类型名称={}", behaviorType.getTypeName());
                return ResponseDTO.error("行为类型添加失败");
            }
        } catch (Exception e) {
            log.error("行为类型添加异常: ", e);
            return ResponseDTO.error("行为类型添加异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseDTO updateBehaviorType(BehaviorType behaviorType) {
        try {
            // 参数校验
            if (behaviorType == null) {
                return ResponseDTO.error("行为类型信息不能为空");
            }
            
            if (behaviorType.getTypeId() == null) {
                return ResponseDTO.error("行为类型ID不能为空");
            }

            int result = behaviorMapper.updateBehaviorType(behaviorType);
            if (result > 0) {
                log.info("行为类型更新成功：类型ID={}", behaviorType.getTypeId());
                return ResponseDTO.success("行为类型更新成功");
            } else {
                log.warn("行为类型更新失败：类型ID={}", behaviorType.getTypeId());
                return ResponseDTO.error("行为类型更新失败");
            }
        } catch (Exception e) {
            log.error("行为类型更新异常: ", e);
            return ResponseDTO.error("行为类型更新异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseDTO deleteBehaviorType(Integer typeId) {
        try {
            // 参数校验
            if (typeId == null) {
                return ResponseDTO.error("行为类型ID不能为空");
            }

            int result = behaviorMapper.deleteBehaviorType(typeId);
            if (result > 0) {
                log.info("行为类型删除成功：类型ID={}", typeId);
                return ResponseDTO.success("行为类型删除成功");
            } else {
                log.warn("行为类型删除失败：类型ID={}", typeId);
                return ResponseDTO.error("行为类型删除失败");
            }
        } catch (Exception e) {
            log.error("行为类型删除异常: ", e);
            return ResponseDTO.error("行为类型删除异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseDTO updateBehaviorRecord(Behavior record) {
        try {
            // 参数校验
            if (record == null) {
                return ResponseDTO.error("行为记录信息不能为空");
            }
            
            if (record.getRecordId() == null) {
                return ResponseDTO.error("记录ID不能为空");
            }

            record.setUpdateTime(LocalDateTime.now());
            int result = behaviorMapper.updateBehaviorRecord(record);
            if (result > 0) {
                log.info("行为记录更新成功：记录ID={}", record.getRecordId());
                return ResponseDTO.success("行为记录更新成功");
            } else {
                log.warn("行为记录更新失败：记录ID={}", record.getRecordId());
                return ResponseDTO.error("行为记录更新失败");
            }
        } catch (Exception e) {
            log.error("行为记录更新异常: ", e);
            return ResponseDTO.error("行为记录更新异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseDTO deleteBehaviorRecord(Long recordId) {
        try {
            // 参数校验
            if (recordId == null) {
                return ResponseDTO.error("记录ID不能为空");
            }

            int result = behaviorMapper.deleteBehaviorRecord(recordId);
            if (result > 0) {
                log.info("行为记录删除成功：记录ID={}", recordId);
                return ResponseDTO.success("行为记录删除成功");
            } else {
                log.warn("行为记录删除失败：记录ID={}", recordId);
                return ResponseDTO.error("行为记录删除失败");
            }
        } catch (Exception e) {
            log.error("行为记录删除异常: ", e);
            return ResponseDTO.error("行为记录删除异常: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getBehaviorTypeDistribution(Integer userId, LocalDate startDate, LocalDate endDate) {
        try {
            if (userId == null || startDate == null || endDate == null) {
                return List.of();
            }
            return behaviorMapper.selectBehaviorTypeDistribution(userId, startDate, endDate);
        } catch (Exception e) {
            log.error("获取行为类型分布异常: userId={}, startDate={}, endDate={}", userId, startDate, endDate, e);
            return List.of();
        }
    }

    @Override
    public Map<String, Object> getWeeklyStatistics(Integer userId) {
        Map<String, Object> statistics = new HashMap<>();
        try {
            if (userId == null) {
                return statistics;
            }

            // 计算本周的开始和结束日期
            LocalDate today = LocalDate.now();
            LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

            // 获取本周总时长
            Integer totalDuration = behaviorMapper.selectTotalDurationByUserAndDate(userId, weekStart, weekEnd);
            totalDuration = totalDuration != null ? totalDuration : 0;

            // 获取本周行为记录数量
            List<Behavior> weeklyRecords = behaviorMapper.selectBehaviorRecordsByUserAndDate(userId, weekStart, weekEnd);
            int recordCount = weeklyRecords != null ? weeklyRecords.size() : 0;

            statistics.put("weekStart", weekStart);
            statistics.put("weekEnd", weekEnd);
            statistics.put("totalDuration", totalDuration);
            statistics.put("recordCount", recordCount);
            statistics.put("averageDuration", recordCount > 0 ? totalDuration / recordCount : 0);

            return statistics;
        } catch (Exception e) {
            log.error("获取周统计异常: userId={}", userId, e);
            return statistics;
        }
    }

    @Override
    public Map<String, Object> getMonthlyStatistics(Integer userId) {
        Map<String, Object> statistics = new HashMap<>();
        try {
            if (userId == null) {
                return statistics;
            }

            // 计算本月的开始和结束日期
            LocalDate today = LocalDate.now();
            LocalDate monthStart = today.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate monthEnd = today.with(TemporalAdjusters.lastDayOfMonth());

            // 获取本月总时长
            Integer totalDuration = behaviorMapper.selectTotalDurationByUserAndDate(userId, monthStart, monthEnd);
            totalDuration = totalDuration != null ? totalDuration : 0;

            // 获取本月行为记录数量
            List<Behavior> monthlyRecords = behaviorMapper.selectBehaviorRecordsByUserAndDate(userId, monthStart, monthEnd);
            int recordCount = monthlyRecords != null ? monthlyRecords.size() : 0;

            statistics.put("monthStart", monthStart);
            statistics.put("monthEnd", monthEnd);
            statistics.put("totalDuration", totalDuration);
            statistics.put("recordCount", recordCount);
            statistics.put("averageDuration", recordCount > 0 ? totalDuration / recordCount : 0);

            return statistics;
        } catch (Exception e) {
            log.error("获取月统计异常: userId={}", userId, e);
            return statistics;
        }
    }
    
    @Override
    public List<Behavior> getAllBehaviors() {
        try {
            return behaviorMapper.selectAllBehaviorRecords();
        } catch (Exception e) {
            log.error("获取所有行为记录异常", e);
            return List.of();
        }
    }
}