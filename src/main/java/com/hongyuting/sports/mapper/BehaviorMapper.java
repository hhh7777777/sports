package com.hongyuting.sports.mapper;

import com.hongyuting.sports.entity.Behavior;
import com.hongyuting.sports.entity.BehaviorType;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 行为记录映射接口
 */
public interface BehaviorMapper extends BaseMapper<Behavior, Long> {

    /**
     * 根据用户ID查询行为记录
     */
    List<Behavior> selectByUserId(@Param("userId") Integer userId);

    /**
     * 根据日期查询行为记录
     */
    List<Behavior> selectByDate(@Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);

    /**
     * 查询用户行为总时长
     */
    Integer selectTotalDurationByUserAndDate(@Param("userId") Integer userId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    /**
     * 查询用户行为类型分布
     */
    List<Map<String, Object>> selectBehaviorTypeDistribution(@Param("userId") Integer userId,
                                                             @Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);

    /**
     * 根据类型和日期统计行为数量
     */
    Integer countByTypeAndDate(@Param("typeId") Integer typeId,
                               @Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate);

    /**
     * 查询最近行为记录
     */
    List<Behavior> selectRecentBehaviors(@Param("limit") int limit);

    /**
     * 根据类型ID查询行为记录
     */
    List<Behavior> selectByTypeId(@Param("typeId") Integer typeId);

    /**
     * 根据用户ID和类型ID查询行为记录
     */
    List<Behavior> selectByUserAndTypeId(@Param("userId") Integer userId,
                                         @Param("typeId") Integer typeId);
    /**
     * 根据用户ID和日期查询行为记录
     */
    List<Behavior> selectByUserAndDate(@Param("userId") Integer userId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);
    /**
     * 根据类型ID和日期查询行为记录
     */
    List<Behavior> selectByTypeAndDate(@Param("typeId") Integer typeId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    /**
     * 根据用户ID、类型ID和日期查询行为记录
     */
    List<Behavior> selectByUserTypeAndDate(@Param("userId") Integer userId,
                                           @Param("typeId") Integer typeId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /**
     * 查询所有行为类型
     */
    List<BehaviorType> selectAllBehaviorTypes();

    /**
     * 根据ID查询行为类型
     */
    BehaviorType selectBehaviorTypeById(@Param("typeId") Integer typeId);

    /**
     * 插入行为类型
     */
    int insertBehaviorType(BehaviorType behaviorType);

    /**
     * 更新行为类型
     */
    int updateBehaviorType(BehaviorType behaviorType);

    /**
     * 删除行为类型
     */
    int deleteBehaviorType(@Param("typeId") Integer typeId);
    
    /**
     * 查询活跃度排行
     */
    List<Map<String, Object>> selectActivityRank(@Param("limit") Integer limit);
    
    /**
     * 根据日期统计活跃用户数量
     */
    int selectActiveUserCountByDate(@Param("date") LocalDate date);
}