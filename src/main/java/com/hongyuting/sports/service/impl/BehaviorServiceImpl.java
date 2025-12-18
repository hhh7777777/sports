package com.hongyuting.sports.service.impl;

import com.hongyuting.sports.dto.BehaviorDTO;
import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.Behavior;
import com.hongyuting.sports.entity.BehaviorType;
import com.hongyuting.sports.mapper.BehaviorMapper;
import com.hongyuting.sports.mapper.BehaviorRecordMapper;
import com.hongyuting.sports.service.BehaviorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BehaviorServiceImpl implements BehaviorService {

    private final BehaviorRecordMapper behaviorRecordMapper;
    private final BehaviorMapper behaviorMapper;

    @Override
    @Transactional
    public ResponseDTO addBehaviorRecord(BehaviorDTO behaviorDTO) {
        try {
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
            return result > 0 ? ResponseDTO.success("行为记录添加成功", record.getRecordId())
                    : ResponseDTO.error("行为记录添加失败");
        } catch (Exception e) {
            return ResponseDTO.error("行为记录添加异常: " + e.getMessage());
        }
    }

    @Override
    public Behavior getBehaviorRecordById(Long recordId) {
        return behaviorRecordMapper.selectBehaviorRecordById(recordId);
    }

    @Override
    public List<Behavior> getBehaviorRecordsByUser(Integer userId) {
        return behaviorRecordMapper.selectBehaviorRecordsByUserId(userId);
    }

    @Override
    public List<Behavior> getBehaviorRecordsByUserAndDate(Integer userId, LocalDate startDate, LocalDate endDate) {
        return behaviorRecordMapper.selectBehaviorRecordsByUserAndDate(userId, startDate, endDate);
    }

    @Override
    public List<Behavior> getTodayBehaviorRecords(Integer userId) {
        LocalDate today = LocalDate.now();
        return getBehaviorRecordsByUserAndDate(userId, today, today);
    }

    @Override
    public Integer getTotalBehaviorDuration(Integer userId, LocalDate startDate, LocalDate endDate) {
        Integer duration = behaviorRecordMapper.selectTotalDurationByUserAndDate(userId, startDate, endDate);
        return duration != null ? duration : 0;
    }

    @Override
    public List<BehaviorType> getAllBehaviorTypes() {
        return behaviorMapper.selectAllBehaviorTypes();
    }

    @Override
    public BehaviorType getBehaviorTypeById(Integer typeId) {
        return behaviorMapper.selectBehaviorTypeById(typeId);
    }

    @Override
    @Transactional
    public ResponseDTO addBehaviorType(BehaviorType behaviorType) {
        try {
            behaviorType.setStatus(1);
            behaviorType.setCreateTime(LocalDateTime.now());
            int result = behaviorMapper.insertBehaviorType(behaviorType);
            return result > 0 ? ResponseDTO.success("行为类型添加成功", behaviorType.getTypeId())
                    : ResponseDTO.error("行为类型添加失败");
        } catch (Exception e) {
            return ResponseDTO.error("行为类型添加异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseDTO updateBehaviorType(BehaviorType behaviorType) {
        try {
            int result = behaviorMapper.updateBehaviorType(behaviorType);
            return result > 0 ? ResponseDTO.success("行为类型更新成功") : ResponseDTO.error("行为类型更新失败");
        } catch (Exception e) {
            return ResponseDTO.error("行为类型更新异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseDTO deleteBehaviorType(Integer typeId) {
        try {
            int result = behaviorMapper.deleteBehaviorType(typeId);
            return result > 0 ? ResponseDTO.success("行为类型删除成功") : ResponseDTO.error("行为类型删除失败");
        } catch (Exception e) {
            return ResponseDTO.error("行为类型删除异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseDTO updateBehaviorRecord(Behavior record) {
        try {
            record.setUpdateTime(LocalDateTime.now());
            int result = behaviorRecordMapper.updateBehaviorRecord(record);
            return result > 0 ? ResponseDTO.success("行为记录更新成功") : ResponseDTO.error("行为记录更新失败");
        } catch (Exception e) {
            return ResponseDTO.error("行为记录更新异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseDTO deleteBehaviorRecord(Long recordId) {
        try {
            int result = behaviorRecordMapper.deleteBehaviorRecord(recordId);
            return result > 0 ? ResponseDTO.success("行为记录删除成功") : ResponseDTO.error("行为记录删除失败");
        } catch (Exception e) {
            return ResponseDTO.error("行为记录删除异常: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getBehaviorTypeDistribution(Integer userId, LocalDate startDate, LocalDate endDate) {
        try {
            return behaviorRecordMapper.selectBehaviorTypeDistribution(userId, startDate, endDate);
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public Integer getTotalBehaviorRecords() {
        try {
            List<Behavior> records = behaviorRecordMapper.selectAllBehaviorRecords();
            return records != null ? records.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}