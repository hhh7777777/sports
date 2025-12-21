package com.hongyuting.sports.service.impl;

import com.hongyuting.sports.dto.BehaviorDTO;
import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.Behavior;
import com.hongyuting.sports.entity.BehaviorType;
import com.hongyuting.sports.mapper.BehaviorMapper;
import com.hongyuting.sports.mapper.BehaviorRecordMapper;
import com.hongyuting.sports.service.BehaviorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * 行为记录服务实现类
 */
public class BehaviorServiceImpl implements BehaviorService {

    private final BehaviorRecordMapper behaviorRecordMapper;
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
            record.setCreateTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());

            int result = behaviorRecordMapper.insertBehaviorRecord(record);
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
            return behaviorRecordMapper.selectBehaviorRecordById(recordId);
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
            return behaviorRecordMapper.selectBehaviorRecordsByUserId(userId);
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
            return behaviorRecordMapper.selectBehaviorRecordsByUserAndDate(userId, startDate, endDate);
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
            Integer duration = behaviorRecordMapper.selectTotalDurationByUserAndDate(userId, startDate, endDate);
            return duration != null ? duration : 0;
        } catch (Exception e) {
            log.error("获取行为总时长异常: userId={}, startDate={}, endDate={}", userId, startDate, endDate, e);
            return 0;
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
            int result = behaviorRecordMapper.updateBehaviorRecord(record);
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

            int result = behaviorRecordMapper.deleteBehaviorRecord(recordId);
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
            return behaviorRecordMapper.selectBehaviorTypeDistribution(userId, startDate, endDate);
        } catch (Exception e) {
            log.error("获取行为类型分布异常: userId={}, startDate={}, endDate={}", userId, startDate, endDate, e);
            return List.of();
        }
    }

    @Override
    public Integer getTotalBehaviorRecords() {
        try {
            List<Behavior> records = behaviorRecordMapper.selectAllBehaviorRecords();
            return records != null ? records.size() : 0;
        } catch (Exception e) {
            log.error("获取行为记录总数异常", e);
            return 0;
        }
    }
}