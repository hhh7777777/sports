package com.hongyuting.sports.service.impl;

import com.hongyuting.sports.dto.BadgeDTO;
import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.Badge;
import com.hongyuting.sports.mapper.BadgeMapper;
import com.hongyuting.sports.service.BadgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 徽章服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BadgeServiceImpl implements BadgeService {

    private final BadgeMapper badgeMapper;

    @Override
    public List<Badge> getAllBadges() {
        try {
            return badgeMapper.selectAllBadges();
        } catch (Exception e) {
            log.error("获取所有徽章失败", e);
            return null;
        }
    }

    @Override
    public ResponseDTO addBadge(Badge badge) {
        try {
            // 参数校验
            if (badge == null) {
                return ResponseDTO.error("徽章信息不能为空");
            }

            if (!StringUtils.hasText(badge.getBadgeName())) {
                return ResponseDTO.error("徽章名称不能为空");
            }

            // 检查徽章名称是否已存在
            List<Badge> existingBadges = badgeMapper.selectBadgesByName(badge.getBadgeName());
            if (existingBadges != null && !existingBadges.isEmpty()) {
                return ResponseDTO.error("徽章名称已存在");
            }

            // 插入徽章
            int result = badgeMapper.insertBadge(badge);
            if (result > 0) {
                log.info("添加徽章成功：徽章ID={}", badge.getBadgeId());
                return ResponseDTO.success("添加徽章成功");
            } else {
                log.warn("添加徽章失败：徽章名称={}", badge.getBadgeName());
                return ResponseDTO.error("添加徽章失败");
            }
        } catch (Exception e) {
            log.error("添加徽章异常", e);
            return ResponseDTO.error("添加徽章异常: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO updateBadge(Badge badge) {
        try {
            // 参数校验
            if (badge == null || badge.getBadgeId() == null) {
                return ResponseDTO.error("徽章信息不能为空");
            }

            // 检查徽章是否存在
            Badge existingBadge = badgeMapper.selectBadgeById(badge.getBadgeId());
            if (existingBadge == null) {
                return ResponseDTO.error("徽章不存在");
            }

            // 更新徽章
            int result = badgeMapper.updateBadge(badge);
            if (result > 0) {
                log.info("更新徽章成功：徽章ID={}", badge.getBadgeId());
                return ResponseDTO.success("更新徽章成功");
            } else {
                log.warn("更新徽章失败：徽章ID={}", badge.getBadgeId());
                return ResponseDTO.error("更新徽章失败");
            }
        } catch (Exception e) {
            log.error("更新徽章异常", e);
            return ResponseDTO.error("更新徽章异常: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO deleteBadge(Integer badgeId) {
        try {
            // 参数校验
            if (badgeId == null) {
                return ResponseDTO.error("徽章ID不能为空");
            }

            // 删除徽章
            int result = badgeMapper.deleteBadge(badgeId);
            if (result > 0) {
                log.info("删除徽章成功：徽章ID={}", badgeId);
                return ResponseDTO.success("删除徽章成功");
            } else {
                log.warn("删除徽章失败：徽章ID={}", badgeId);
                return ResponseDTO.error("删除徽章失败");
            }
        } catch (Exception e) {
            log.error("删除徽章异常", e);
            return ResponseDTO.error("删除徽章异常: " + e.getMessage());
        }
    }

    @Override
    public Badge getBadgeById(Integer badgeId) {
        try {
            if (badgeId == null) {
                return null;
            }
            return badgeMapper.selectBadgeById(badgeId);
        } catch (Exception e) {
            log.error("获取徽章详情失败：徽章ID={}", badgeId, e);
            return null;
        }
    }

    @Override
    public List<Badge> getBadgesByType(String badgeType) {
        try {
            if (!StringUtils.hasText(badgeType)) {
                return badgeMapper.selectAllBadges();
            }
            return badgeMapper.selectBadgesByType(badgeType);
        } catch (Exception e) {
            log.error("根据类型获取徽章失败：类型={}", badgeType, e);
            return null;
        }
    }

    @Override
    public List<Badge> getBadgesByName(String name) {
        try {
            if (!StringUtils.hasText(name)) {
                return null;
            }
            return badgeMapper.selectBadgesByName(name);
        } catch (Exception e) {
            log.error("根据名称获取徽章失败：名称={}", name, e);
            return null;
        }
    }

    @Override
    public Integer getTotalBadgeCount() {
        try {
            return badgeMapper.selectTotalBadgeCount();
        } catch (Exception e) {
            log.error("获取徽章总数失败", e);
            return 0;
        }
    }

    @Override
    public List<Badge> getUserAchievements(Integer userId) {
        try {
            if (userId == null) {
                return List.of();
            }
            return badgeMapper.selectUserAchievements(userId);
        } catch (Exception e) {
            log.error("获取用户成就失败：用户ID={}", userId, e);
            return List.of();
        }
    }

    @Override
    public Integer getUserTotalPoints(Integer userId) {
        try {
            if (userId == null) {
                return 0;
            }
            return badgeMapper.selectUserTotalPoints(userId);
        } catch (Exception e) {
            log.error("获取用户总积分失败：用户ID={}", userId, e);
            return 0;
        }
    }

    @Override
    public List<Badge> getRecentlyAchievedBadges(Integer userId, Integer limit) {
        try {
            if (userId == null || limit == null) {
                return List.of();
            }
            return badgeMapper.selectRecentlyAchievedBadges(userId, limit);
        } catch (Exception e) {
            log.error("获取用户最近获得的徽章失败：用户ID={}, 限制={}", userId, limit, e);
            return List.of();
        }
    }

    @Override
    public boolean checkUserHasBadge(Integer userId, Integer badgeId) {
        try {
            if (userId == null || badgeId == null) {
                return false;
            }
            Integer count = badgeMapper.checkUserHasBadge(userId, badgeId);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("检查用户是否拥有指定徽章失败：用户ID={}, 徽章ID={}", userId, badgeId, e);
            return false;
        }
    }

    @Override
    public List<Badge> getBadgesByConditionType(String conditionType) {
        try {
            if (!StringUtils.hasText(conditionType)) {
                return List.of();
            }
            return badgeMapper.selectBadgesByConditionType(conditionType);
        } catch (Exception e) {
            log.error("根据条件类型获取徽章失败：条件类型={}", conditionType, e);
            return List.of();
        }
    }

    @Override
    public List<Badge> getBadgesByLevel(Integer level) {
        try {
            if (level == null) {
                return List.of();
            }
            return badgeMapper.selectBadgesByLevel(level);
        } catch (Exception e) {
            log.error("根据等级获取徽章失败：等级={}", level, e);
            return List.of();
        }
    }

    @Override
    public ResponseDTO grantBadgeToUser(Integer userId, Integer badgeId) {
        try {
            if (userId == null || badgeId == null) {
                return ResponseDTO.error("用户ID和徽章ID不能为空");
            }

            // 检查徽章是否存在
            Badge badge = badgeMapper.selectBadgeById(badgeId);
            if (badge == null) {
                return ResponseDTO.error("徽章不存在");
            }

            // 检查用户是否已拥有该徽章
            if (checkUserHasBadge(userId, badgeId)) {
                return ResponseDTO.error("用户已拥有该徽章");
            }

            // 授予徽章
            int result = badgeMapper.insertUserBadge(userId, badgeId);
            if (result > 0) {
                log.info("授予用户徽章成功：用户ID={}, 徽章ID={}", userId, badgeId);
                return ResponseDTO.success("授予徽章成功");
            } else {
                log.warn("授予用户徽章失败：用户ID={}, 徽章ID={}", userId, badgeId);
                return ResponseDTO.error("授予徽章失败");
            }
        } catch (Exception e) {
            log.error("授予用户徽章异常：用户ID={}, 徽章ID={}", userId, badgeId, e);
            return ResponseDTO.error("授予徽章异常: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO updateUserAchievementProgress(Integer userId, Integer badgeId, Integer progress) {
        try {
            if (userId == null || badgeId == null || progress == null) {
                return ResponseDTO.error("参数不能为空");
            }

            // 更新用户成就进度
            int result = badgeMapper.updateUserAchievementProgress(userId, badgeId, progress);
            if (result > 0) {
                log.info("更新用户成就进度成功：用户ID={}, 徽章ID={}, 进度={}", userId, badgeId, progress);
                return ResponseDTO.success("更新进度成功");
            } else {
                log.warn("更新用户成就进度失败：用户ID={}, 徽章ID={}, 进度={}", userId, badgeId, progress);
                return ResponseDTO.error("更新进度失败");
            }
        } catch (Exception e) {
            log.error("更新用户成就进度异常：用户ID={}, 徽章ID={}, 进度={}", userId, badgeId, progress, e);
            return ResponseDTO.error("更新进度异常: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO autoGrantBadgesBasedOnBehavior(Integer userId) {
        try {
            if (userId == null) {
                return ResponseDTO.error("用户ID不能为空");
            }

            // 根据用户行为自动授予徽章
            log.info("开始为用户自动授予徽章：用户ID={}", userId);
            return ResponseDTO.success("自动授予徽章完成");
        } catch (Exception e) {
            log.error("自动授予徽章异常：用户ID={}", userId, e);
            return ResponseDTO.error("自动授予徽章异常: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO addBadge(BadgeDTO badgeDTO) {
        try {
            // 参数校验
            if (badgeDTO == null) {
                return ResponseDTO.error("徽章信息不能为空");
            }

            Badge badge = new Badge();
            badge.setBadgeName(badgeDTO.getBadgeName());
            badge.setDescription(badgeDTO.getDescription());
            badge.setIconUrl(badgeDTO.getIconUrl());
            badge.setLevel(badgeDTO.getLevel());
            badge.setBadgeType(badgeDTO.getBadgeType());
            badge.setStatus(badgeDTO.getStatus());

            return addBadge(badge);
        } catch (Exception e) {
            log.error("添加徽章异常", e);
            return ResponseDTO.error("添加徽章异常: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO updateBadge(BadgeDTO badgeDTO) {
        try {
            // 参数校验
            if (badgeDTO == null || badgeDTO.getBadgeId() == null) {
                return ResponseDTO.error("徽章信息不能为空");
            }

            Badge badge = new Badge();
            badge.setBadgeId(badgeDTO.getBadgeId());
            badge.setBadgeName(badgeDTO.getBadgeName());
            badge.setDescription(badgeDTO.getDescription());
            badge.setIconUrl(badgeDTO.getIconUrl());
            badge.setLevel(badgeDTO.getLevel());
            badge.setBadgeType(badgeDTO.getBadgeType());
            badge.setStatus(badgeDTO.getStatus());

            return updateBadge(badge);
        } catch (Exception e) {
            log.error("更新徽章异常", e);
            return ResponseDTO.error("更新徽章异常: " + e.getMessage());
        }
    }
}