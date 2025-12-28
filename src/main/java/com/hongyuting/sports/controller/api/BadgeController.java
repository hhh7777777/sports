package com.hongyuting.sports.controller.api;

import com.hongyuting.sports.dto.BadgeDTO;
import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.Badge;
import com.hongyuting.sports.entity.UserBadge;
import com.hongyuting.sports.service.BadgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/badge")
@RequiredArgsConstructor
@Slf4j
public class BadgeController {
    /**
     * 徽章服务类
     */
    private final BadgeService badgeService;

    /**
     * 获取所有徽章
     */
    @GetMapping("/list")
    public ResponseDTO getAllBadges() {
        List<Badge> badges = badgeService.getAllBadges();
        return getResponseDTO(badges);
    }
    /**
     * 获取所有徽章的DTO列表
     */
    private ResponseDTO getResponseDTO(List<Badge> badges) {
        List<BadgeDTO> badgeDTOs = badges.stream().map(badge -> {
            BadgeDTO dto = new BadgeDTO();
            dto.setBadgeId(badge.getBadgeId());
            dto.setBadgeName(badge.getBadgeName());
            dto.setDescription(badge.getDescription());
            dto.setIconUrl(badge.getIconUrl());
            dto.setConditionType(badge.getConditionType());
            dto.setConditionValue(badge.getConditionValue());
            dto.setLevel(badge.getLevel());
            dto.setRewardPoints(badge.getRewardPoints());
            dto.setStatus(badge.getStatus());
            dto.setBadgeType(badge.getBadgeType());
            return dto;
        }).collect(Collectors.toList());
        return ResponseDTO.success("获取成功", badgeDTOs);
    }

    /**
     * 获取徽章详情
     */
    @GetMapping("/{badgeId}")
    public ResponseDTO getBadgeById(@PathVariable Integer badgeId) {
        Badge badge = badgeService.getBadgeById(badgeId);
        if (badge == null) {
            return ResponseDTO.error("徽章不存在");
        }
        BadgeDTO badgeDTO = new BadgeDTO();
        badgeDTO.setBadgeId(badge.getBadgeId());
        badgeDTO.setBadgeName(badge.getBadgeName());
        badgeDTO.setDescription(badge.getDescription());
        badgeDTO.setIconUrl(badge.getIconUrl());
        badgeDTO.setConditionType(badge.getConditionType());
        badgeDTO.setConditionValue(badge.getConditionValue());
        badgeDTO.setLevel(badge.getLevel());
        badgeDTO.setRewardPoints(badge.getRewardPoints());
        badgeDTO.setStatus(badge.getStatus());
        badgeDTO.setBadgeType(badge.getBadgeType()); // 添加徽章类型

        return ResponseDTO.success("获取成功", badgeDTO);
    }

    /**
     * 获取当前用户的成就
     */
    @GetMapping("/my-achievements")
    public ResponseDTO getMyAchievements(@RequestAttribute Integer userId) {
        try {
            // 获取用户已获得的徽章及其获得时间
            List<UserBadge> userBadges = badgeService.getUserAchievementDetails(userId);
            
            // 获取所有徽章
            List<Badge> allBadges = badgeService.getAllBadges();
            
            // 创建一个映射，便于查找用户获得的徽章及其获得时间
            Map<Integer, UserBadge> userBadgesMap = new HashMap<>();
            if (userBadges != null) {
                for (UserBadge userBadge : userBadges) {
                    userBadgesMap.put(userBadge.getBadgeId(), userBadge);
                }
            }
            
            // 构建徽章列表，标记哪些是用户已获得的
            List<Map<String, Object>> badgeList = new ArrayList<>();
            for (Badge badge : allBadges) {
                Map<String, Object> badgeInfo = new HashMap<>();
                badgeInfo.put("badgeId", badge.getBadgeId());
                badgeInfo.put("badgeName", badge.getBadgeName());
                badgeInfo.put("description", badge.getDescription());
                badgeInfo.put("iconUrl", badge.getIconUrl());
                badgeInfo.put("level", badge.getLevel());
                badgeInfo.put("rewardPoints", badge.getRewardPoints());
                badgeInfo.put("status", badge.getStatus());
                badgeInfo.put("badgeType", badge.getBadgeType());
                
                // 判断用户是否已获得该徽章
                UserBadge userBadge = userBadgesMap.get(badge.getBadgeId());
                boolean achieved = userBadge != null;
                badgeInfo.put("achieved", achieved);
                badgeInfo.put("progress", achieved ? userBadge.getProgress() : 0); // 如果已获得则使用实际进度
                
                // 添加获得时间信息
                if (achieved && userBadge.getAchieveTime() != null) {
                    badgeInfo.put("achieveTime", userBadge.getAchieveTime());
                }
                
                badgeList.add(badgeInfo);
            }
            
            return ResponseDTO.success("获取成功", badgeList);
        } catch (Exception e) {
            log.error("获取用户成就异常，用户ID: {}", userId, e);
            return ResponseDTO.error("获取用户成就异常: " + e.getMessage());
        }
    }

    /**
     * 获取用户总积分
     */
    @GetMapping("/my-points")
    public ResponseDTO getMyTotalPoints(@RequestAttribute Integer userId) {
        Integer totalPoints = badgeService.getUserTotalPoints(userId);
        return ResponseDTO.success("获取成功", totalPoints);
    }

    /**
     * 获取用户最近获得的成就
     */
    @GetMapping("/recent-achievements")
    public ResponseDTO getRecentAchievements(@RequestAttribute Integer userId,
                                                            @RequestParam(defaultValue = "5") Integer limit) {
        List<Badge> achievements = badgeService.getRecentlyAchievedBadges(userId, limit);
        // 将Badge转换为UserBadge格式的DTO，这里我们直接返回Badge信息
        return ResponseDTO.success("获取成功", achievements);
    }

    /**
     * 检查用户是否拥有某个徽章
     */
    @GetMapping("/check/{badgeId}")
    public ResponseDTO checkBadgeOwnership(@PathVariable Integer badgeId,
                                                          @RequestAttribute Integer userId) {
        boolean hasBadge = badgeService.checkUserHasBadge(userId, badgeId);
        return ResponseDTO.success("查询成功", hasBadge);
    }

    /**
     * 管理员：添加徽章
     */
    @PostMapping("/admin")
    public ResponseDTO addBadge(@RequestBody BadgeDTO badgeDTO) {
        Badge badge = new Badge();
        badge.setBadgeName(badgeDTO.getBadgeName());
        badge.setDescription(badgeDTO.getDescription());
        badge.setIconUrl(badgeDTO.getIconUrl());
        badge.setConditionType(badgeDTO.getConditionType());
        badge.setConditionValue(badgeDTO.getConditionValue());
        badge.setLevel(badgeDTO.getLevel());
        badge.setRewardPoints(badgeDTO.getRewardPoints());
        badge.setStatus(badgeDTO.getStatus());
        badge.setBadgeType(badgeDTO.getBadgeType());
        return badgeService.addBadge(badge);
    }

    /**
     * 管理员：更新徽章
     */
    @PutMapping("/admin")
    public ResponseDTO updateBadge(@RequestBody BadgeDTO badgeDTO) {
        Badge badge = new Badge();
        badge.setBadgeId(badgeDTO.getBadgeId());
        badge.setBadgeName(badgeDTO.getBadgeName());
        badge.setDescription(badgeDTO.getDescription());
        badge.setIconUrl(badgeDTO.getIconUrl());
        badge.setConditionType(badgeDTO.getConditionType());
        badge.setConditionValue(badgeDTO.getConditionValue());
        badge.setLevel(badgeDTO.getLevel());
        badge.setRewardPoints(badgeDTO.getRewardPoints());
        badge.setStatus(badgeDTO.getStatus());
        badge.setBadgeType(badgeDTO.getBadgeType()); // 添加徽章类型

        return badgeService.updateBadge(badge);
    }

    /**
     * 管理员：删除徽章
     */
    @DeleteMapping("/admin/{badgeId}")
    public ResponseDTO deleteBadge(@PathVariable Integer badgeId) {
        return badgeService.deleteBadge(badgeId);
    }

    /**
     * 管理员：按条件查询徽章
     */
    @GetMapping("/admin/by-condition")
    public ResponseDTO getBadgesByCondition(@RequestParam(required = false) String conditionType,
                                                           @RequestParam(required = false) Integer level) {
        List<Badge> badges;
        if (conditionType != null) {
            badges = badgeService.getBadgesByConditionType(conditionType);
        } else if (level != null) {
            badges = badgeService.getBadgesByLevel(level);
        } else {
            badges = badgeService.getAllBadges();
        }

        return getResponseDTO(badges);
    }

    /**
     * 管理员：授予徽章给用户
     */
    @PostMapping("/admin/grant")
    public ResponseDTO grantBadgeToUser(@RequestParam Integer userId,
                                                       @RequestParam Integer badgeId) {
        return badgeService.grantBadgeToUser(userId, badgeId);
    }

    /**
     * 管理员：更新用户成就进度
     */
    @PutMapping("/admin/progress")
    public ResponseDTO updateUserAchievementProgress(@RequestParam Integer userId,
                                                                    @RequestParam Integer badgeId,
                                                                    @RequestParam Integer progress) {
        return badgeService.updateUserAchievementProgress(userId, badgeId, progress);
    }

    /**
     * 管理员：获取用户的成就列表
     */
    @GetMapping("/admin/user-achievements")
    public ResponseDTO getUserAchievements(@RequestParam Integer userId) {
        List<Badge> achievements = badgeService.getUserAchievements(userId);
        return ResponseDTO.success("获取成功", achievements);
    }

    /**
     * 自动授予徽章（基于用户行为）
     */
    @PostMapping("/auto-grant")
    public ResponseDTO autoGrantBadges(@RequestParam Integer userId) {
        return badgeService.autoGrantBadgesBasedOnBehavior(userId);
    }
}