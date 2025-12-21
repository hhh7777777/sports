package com.hongyuting.sports.service.impl;

import com.hongyuting.sports.dto.ResponseDTO;
import com.hongyuting.sports.entity.Badge;
import com.hongyuting.sports.entity.UserBadge;
import com.hongyuting.sports.mapper.AchievementBadgeMapper;
import com.hongyuting.sports.mapper.UserAchievementMapper;
import com.hongyuting.sports.service.BadgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * 徽章服务实现类
 */
public class BadgeServiceImpl implements BadgeService {

    private final AchievementBadgeMapper achievementBadgeMapper;
    private final UserAchievementMapper userAchievementMapper;

    @Override
    public List<Badge> getBadgesByConditionType(String conditionType) {
        try {
            if (!StringUtils.hasText(conditionType)) {
                return List.of();
            }
            return achievementBadgeMapper.selectBadgesByConditionType(conditionType);
        } catch (Exception e) {
            log.error("根据条件类型获取徽章异常: conditionType={}", conditionType, e);
            return List.of();
        }
    }

    @Override
    public List<UserBadge> getUserAchievements(Integer userId) {
        try {
            if (userId == null) {
                return List.of();
            }
            return userAchievementMapper.selectUserBadgesByUserId(userId);
        } catch (Exception e) {
            log.error("获取用户成就异常: userId={}", userId, e);
            return List.of();
        }
    }

    @Override
    public Integer getUserTotalPoints(Integer userId) {
        try {
            if (userId == null) {
                return 0;
            }
            Integer totalPoints = userAchievementMapper.sumPointsByUserId(userId);
            return totalPoints != null ? totalPoints : 0;
        } catch (Exception e) {
            log.error("获取用户总积分异常: userId={}", userId, e);
            return 0;
        }
    }

    @Override
    @Transactional
    public ResponseDTO grantBadgeToUser(Integer userId, Integer badgeId) {
        try {
            // 参数校验
            if (userId == null) {
                return ResponseDTO.error("用户ID不能为空");
            }
            
            if (badgeId == null) {
                return ResponseDTO.error("徽章ID不能为空");
            }

            // 检查用户是否已拥有该徽章
            if (checkUserHasBadge(userId, badgeId)) {
                return ResponseDTO.error("用户已拥有该徽章");
            }

            // 检查徽章是否存在
            Badge badge = achievementBadgeMapper.selectBadgeById(badgeId);
            if (badge == null) {
                return ResponseDTO.error("徽章不存在");
            }

            UserBadge userBadge = new UserBadge();
            userBadge.setUserId(userId);
            userBadge.setBadgeId(badgeId);
            userBadge.setAchieveTime(LocalDateTime.now());
            userBadge.setProgress(100); // 授予徽章时进度为100%
            userBadge.setUpdateTime(LocalDateTime.now());

            int result = userAchievementMapper.insertUserBadge(userBadge);
            if (result > 0) {
                log.info("徽章授予成功：用户ID={}，徽章ID={}", userId, badgeId);
                return ResponseDTO.success("徽章授予成功");
            } else {
                log.warn("徽章授予失败：用户ID={}，徽章ID={}", userId, badgeId);
                return ResponseDTO.error("徽章授予失败");
            }
        } catch (Exception e) {
            log.error("授予徽章异常: userId={}, badgeId={}", userId, badgeId, e);
            return ResponseDTO.error("徽章授予异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseDTO updateUserAchievementProgress(Integer userId, Integer badgeId, Integer progress) {
        try {
            // 参数校验
            if (userId == null) {
                return ResponseDTO.error("用户ID不能为空");
            }
            
            if (badgeId == null) {
                return ResponseDTO.error("徽章ID不能为空");
            }
            
            if (progress == null) {
                return ResponseDTO.error("进度不能为空");
            }

            UserBadge userBadge = userAchievementMapper.selectUserBadge(userId, badgeId);
            if (userBadge == null) {
                return ResponseDTO.error("用户未拥有该徽章");
            }

            // 确保进度在0-100之间
            int newProgress = Math.max(0, Math.min(100, progress));
            userBadge.setProgress(newProgress);
            userBadge.setUpdateTime(LocalDateTime.now());

            int result = userAchievementMapper.updateUserAchievement(userBadge);
            if (result > 0) {
                log.info("徽章进度更新成功：用户ID={}，徽章ID={}，进度={}", userId, badgeId, newProgress);
                return ResponseDTO.success("徽章进度更新成功");
            } else {
                log.warn("徽章进度更新失败：用户ID={}，徽章ID={}，进度={}", userId, badgeId, newProgress);
                return ResponseDTO.error("徽章进度更新失败");
            }
        } catch (Exception e) {
            log.error("更新徽章进度异常: userId={}, badgeId={}, progress={}", userId, badgeId, progress, e);
            return ResponseDTO.error("徽章进度更新异常: " + e.getMessage());
        }
    }

    @Override
    public boolean checkUserHasBadge(Integer userId, Integer badgeId) {
        try {
            if (userId == null || badgeId == null) {
                return false;
            }
            
            UserBadge userBadge = userAchievementMapper.selectUserBadge(userId, badgeId);
            return userBadge != null && userBadge.getProgress() != null && userBadge.getProgress() >= 100;
        } catch (Exception e) {
            log.error("检查用户是否拥有徽章异常: userId={}, badgeId={}", userId, badgeId, e);
            return false;
        }
    }

    @Override
    public List<UserBadge> getRecentlyAchievedBadges(Integer userId, Integer limit) {
        try {
            if (userId == null) {
                return List.of();
            }
            
            // 设置默认限制
            if (limit == null || limit <= 0) {
                limit = 5;
            }
            
            return userAchievementMapper.selectRecentlyAchievedBadges(userId, limit);
        } catch (Exception e) {
            log.error("获取最近获得的徽章异常: userId={}, limit={}", userId, limit, e);
            return List.of();
        }
    }

    @Override
    public List<Badge> getBadgesByLevel(Integer level) {
        try {
            if (level == null || level <= 0) {
                return List.of();
            }
            return achievementBadgeMapper.selectBadgesByLevel(level);
        } catch (Exception e) {
            log.error("根据等级获取徽章异常: level={}", level, e);
            return List.of();
        }
    }

    @Override
    @Transactional
    public ResponseDTO addBadge(Badge badge) {
        try {
            // 参数校验
            if (badge == null) {
                return ResponseDTO.error("徽章信息不能为空");
            }

            // 验证徽章信息
            if (!StringUtils.hasText(badge.getBadgeName())) {
                return ResponseDTO.error("徽章名称不能为空");
            }
            
            if (badge.getLevel() == null || badge.getLevel() < 1) {
                return ResponseDTO.error("徽章等级必须大于0");
            }

            badge.setStatus(1);
            badge.setCreateTime(LocalDateTime.now());
            badge.setUpdateTime(LocalDateTime.now());

            int result = achievementBadgeMapper.insertBadge(badge);
            if (result > 0) {
                log.info("徽章添加成功：徽章ID={}", badge.getBadgeId());
                return ResponseDTO.success("徽章添加成功", badge.getBadgeId());
            } else {
                log.warn("徽章添加失败：徽章名称={}", badge.getBadgeName());
                return ResponseDTO.error("徽章添加失败");
            }
        } catch (Exception e) {
            log.error("添加徽章异常", e);
            return ResponseDTO.error("徽章添加异常: " + e.getMessage());
        }
    }

    @Override
    public Badge getBadgeById(Integer badgeId) {
        try {
            if (badgeId == null) {
                return null;
            }
            return achievementBadgeMapper.selectBadgeById(badgeId);
        } catch (Exception e) {
            log.error("根据ID获取徽章异常: badgeId={}", badgeId, e);
            return null;
        }
    }

    @Override
    public List<Badge> getAllBadges() {
        try {
            return achievementBadgeMapper.selectAllBadges();
        } catch (Exception e) {
            log.error("获取所有徽章异常", e);
            return List.of();
        }
    }

    @Override
    @Transactional
    public ResponseDTO updateBadge(Badge badge) {
        try {
            // 参数校验
            if (badge == null) {
                return ResponseDTO.error("徽章信息不能为空");
            }
            
            if (badge.getBadgeId() == null) {
                return ResponseDTO.error("徽章ID不能为空");
            }

            Badge existingBadge = achievementBadgeMapper.selectBadgeById(badge.getBadgeId());
            if (existingBadge == null) {
                return ResponseDTO.error("徽章不存在");
            }

            badge.setUpdateTime(LocalDateTime.now());
            int result = achievementBadgeMapper.updateBadge(badge);
            if (result > 0) {
                log.info("徽章更新成功：徽章ID={}", badge.getBadgeId());
                return ResponseDTO.success("徽章更新成功");
            } else {
                log.warn("徽章更新失败：徽章ID={}", badge.getBadgeId());
                return ResponseDTO.error("徽章更新失败");
            }
        } catch (Exception e) {
            log.error("更新徽章异常: badgeId={}", badge != null ? badge.getBadgeId() : null, e);
            return ResponseDTO.error("徽章更新异常: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseDTO deleteBadge(Integer badgeId) {
        try {
            // 参数校验
            if (badgeId == null) {
                return ResponseDTO.error("徽章ID不能为空");
            }

            // 检查是否有用户拥有该徽章
            int userCount = userAchievementMapper.countUsersWithBadge(badgeId);
            if (userCount > 0) {
                return ResponseDTO.error("无法删除徽章，已有" + userCount + "名用户获得该徽章");
            }

            int result = achievementBadgeMapper.deleteBadge(badgeId);
            if (result > 0) {
                log.info("徽章删除成功：徽章ID={}", badgeId);
                return ResponseDTO.success("徽章删除成功");
            } else {
                log.warn("徽章删除失败：徽章ID={}", badgeId);
                return ResponseDTO.error("徽章删除失败");
            }
        } catch (Exception e) {
            log.error("删除徽章异常: badgeId={}", badgeId, e);
            return ResponseDTO.error("徽章删除异常: " + e.getMessage());
        }
    }

    @Override
    public List<UserBadge> getUserBadges(Integer userId) {
        try {
            if (userId == null) {
                return List.of();
            }
            return userAchievementMapper.selectUserBadgesByUserId(userId);
        } catch (Exception e) {
            log.error("获取用户徽章异常: userId={}", userId, e);
            return List.of();
        }
    }

    @Transactional
    @Override
    public ResponseDTO assignBadgeToUser(Integer userId, Integer badgeId) {
        return grantBadgeToUser(userId, badgeId);
    }

    @Transactional
    @Override
    public ResponseDTO updateBadgeProgress(UserBadge userBadge) {
        try {
            if (userBadge == null) {
                return ResponseDTO.error("用户徽章信息不能为空");
            }
            
            userBadge.setUpdateTime(LocalDateTime.now());
            int result = userAchievementMapper.updateUserAchievement(userBadge);
            if (result > 0) {
                log.info("徽章进度更新成功：用户ID={}，徽章ID={}", userBadge.getUserId(), userBadge.getBadgeId());
                return ResponseDTO.success("徽章进度更新成功");
            } else {
                log.warn("徽章进度更新失败：用户ID={}，徽章ID={}", userBadge.getUserId(), userBadge.getBadgeId());
                return ResponseDTO.error("徽章进度更新失败");
            }
        } catch (Exception e) {
            log.error("更新徽章进度异常", e);
            return ResponseDTO.error("徽章进度更新异常: " + e.getMessage());
        }
    }

    @Override
    public Integer getTotalBadgeCount() {
        try {
            Integer count = achievementBadgeMapper.selectTotalBadgeCount();
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("获取徽章总数异常", e);
            return 0;
        }
    }
}