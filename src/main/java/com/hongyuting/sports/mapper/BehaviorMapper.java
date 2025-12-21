package com.hongyuting.sports.mapper;

import com.hongyuting.sports.entity.Behavior;
import com.hongyuting.sports.entity.BehaviorType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 行为映射接口
 */
@Mapper
public interface BehaviorMapper {
    Behavior selectById(Long recordId);

    List<Behavior> selectByUserId(Integer userId);

    List<Behavior> selectByUserIdAndDateRange(@Param("userId") Integer userId,
                                              @Param("startDate") String startDate,
                                              @Param("endDate") String endDate);

    int insert(Behavior behavior);

    int update(Behavior behavior);

    int delete(Long recordId);

    // BehaviorType related methods
    List<BehaviorType> selectAllBehaviorTypes();

    BehaviorType selectBehaviorTypeById(@Param("typeId") Integer typeId);

    int insertBehaviorType(BehaviorType behaviorType);

    int updateBehaviorType(BehaviorType behaviorType);

    int deleteBehaviorType(@Param("typeId") Integer typeId);
}