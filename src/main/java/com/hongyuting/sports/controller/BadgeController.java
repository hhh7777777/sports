package com.hongyuting.sports.controller;

import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.Badge;
import com.hongyuting.sports.entity.UserBadge;
import com.hongyuting.sports.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/badge")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    /**
     * 获取所有徽章
     */
    @GetMapping("/list")
    public ResponseDTO getAllBadges() {
        List<Badge> badges = badgeService.getAllBadges();
        return ResponseDTO.success("获取成功", badges);
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
        return ResponseDTO.success("获取成功", badge);
    }

    /**
     * 获取当前用户的成就
     */
    @GetMapping("/my-achievements")
    public ResponseDTO getMyAchievements(@RequestAttribute Integer userId) {
        List<UserBadge> achievements = badgeService.getUserAchievements(userId);
        return ResponseDTO.success("获取成功", achievements);
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
     * 获取最近获得的成就
     */
    @GetMapping("/recent-achievements")
    public ResponseDTO getRecentAchievements(@RequestAttribute Integer userId,
                                             @RequestParam(defaultValue = "5") Integer limit) {
        List<UserBadge> achievements = badgeService.getRecentlyAchievedBadges(userId, limit);
        return ResponseDTO.success("获取成功", achievements);
    }

    /**
     * 检查是否拥有某个徽章
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
    public ResponseDTO addBadge(@RequestBody Badge badge) {
        return badgeService.addBadge(badge);
    }

    /**
     * 管理员：更新徽章
     */
    @PutMapping("/admin")
    public ResponseDTO updateBadge(@RequestBody Badge badge) {
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
        return ResponseDTO.success("获取成功", badges);
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
        List<UserBadge> achievements = badgeService.getUserAchievements(userId);
        return ResponseDTO.success("获取成功", achievements);
    }
}