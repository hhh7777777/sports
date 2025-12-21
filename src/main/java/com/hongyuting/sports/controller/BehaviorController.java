package com.hongyuting.sports.controller;

import com.hongyuting.sports.dto.BehaviorDTO;
import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.Behavior;
import com.hongyuting.sports.entity.BehaviorType;
import com.hongyuting.sports.service.BehaviorService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/behavior")
@RequiredArgsConstructor
public class BehaviorController {
    /**
     * 行为服务
     */
    private final BehaviorService behaviorService;
    /**
     * 添加行为记录
     */
    @PostMapping("/record")
    public ResponseDTO addBehaviorRecord(@RequestBody BehaviorDTO behaviorDTO) {
        return behaviorService.addBehaviorRecord(behaviorDTO);
    }
    /**
    * 修改行为记录
    */
    @PutMapping("/record")
    public ResponseDTO updateBehaviorRecord(@RequestBody Behavior record) {
        return behaviorService.updateBehaviorRecord(record);
    }
    /**
     * 删除行为记录
     */
    @DeleteMapping("/record/{recordId}")
    public ResponseDTO deleteBehaviorRecord(@PathVariable Long recordId) {
        return behaviorService.deleteBehaviorRecord(recordId);
    }

    /**
     * 获取行为记录
     */
    @GetMapping("/record/{recordId}")
    public ResponseDTO getBehaviorRecordById(@PathVariable Long recordId) {
        Behavior record = behaviorService.getBehaviorRecordById(recordId);
        return record != null ? ResponseDTO.success(String.valueOf(record)) : ResponseDTO.error("记录不存在");
    }
    /**
     * 获取用户行为记录
     */
    @GetMapping("/record/user/{userId}")
    public ResponseDTO getBehaviorRecordsByUser(@PathVariable Integer userId) {
        List<Behavior> records = behaviorService.getBehaviorRecordsByUser(userId);
        return ResponseDTO.success(records.toString());
    }
    /**
     * 获取用户行为记录（指定日期范围）
     */
    @GetMapping("/record/user/{userId}/date-range")
    public ResponseDTO getBehaviorRecordsByDateRange(
            @PathVariable Integer userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Behavior> records = behaviorService.getBehaviorRecordsByUserAndDate(userId, startDate, endDate);
        return ResponseDTO.success(records.toString());
    }
    /**
     * 获取用户今日行为记录
     */
    @GetMapping("/record/user/{userId}/today")
    public ResponseDTO getTodayBehaviorRecords(@PathVariable Integer userId) {
        List<Behavior> records = behaviorService.getTodayBehaviorRecords(userId);
        return ResponseDTO.success(records.toString());
    }
    /**
     * 获取用户总行为时长
     */
    @GetMapping("/record/user/{userId}/total-duration")
    public ResponseDTO getTotalBehaviorDuration(
            @PathVariable Integer userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Integer totalDuration = behaviorService.getTotalBehaviorDuration(userId, startDate, endDate);
        return ResponseDTO.success(String.valueOf(totalDuration));
    }
    /**
     * 获取用户行为类型分布
     */
    @GetMapping("/type")
    public ResponseDTO getAllBehaviorTypes() {
        List<BehaviorType> types = behaviorService.getAllBehaviorTypes();
        return ResponseDTO.success(types.toString());
    }
    /**
     * 添加行为类型
     */
    @PostMapping("/type")
    public ResponseDTO addBehaviorType(@RequestBody BehaviorType behaviorType) {
        return behaviorService.addBehaviorType(behaviorType);
    }
    /**
     * 修改行为类型
     */
    @PutMapping("/type")
    public ResponseDTO updateBehaviorType(@RequestBody BehaviorType behaviorType) {
        return behaviorService.updateBehaviorType(behaviorType);
    }
    /**
     * 删除行为类型
     */
    @DeleteMapping("/type/{typeId}")
    public ResponseDTO deleteBehaviorType(@PathVariable Integer typeId) {
        return behaviorService.deleteBehaviorType(typeId);
    }
    /**
     * 获取行为类型
     */
    @GetMapping("/type/{typeId}")
    public ResponseDTO getBehaviorTypeById(@PathVariable Integer typeId) {
        BehaviorType type = behaviorService.getBehaviorTypeById(typeId);
        return type != null ? ResponseDTO.success(String.valueOf(type)) : ResponseDTO.error("类型不存在");
    }
}