package com.hongyuting.sports.mapper;

import com.hongyuting.sports.entity.BehaviorType;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 行为类型映射接口
 */
public interface BehaviorTypeMapper {
    /**
     * 添加行为类型
     */
    int insertBehaviorType(BehaviorType behaviorType);
    /**
     * 更新行为类型
     */
    int updateBehaviorType(BehaviorType behaviorType);
    /**
     * 禁用行为类型
     */
    int disableBehaviorType(@Param("typeId") int typeId);
    /**
     * 根据ID查询行为类型
     */
    BehaviorType selectBehaviorTypeById(@Param("typeId") int typeId);
    /**
     * 根据名称查询行为类型
     */
    BehaviorType selectBehaviorTypeByName(@Param("typeName") String typeName);
    /**
     * 查询所有行为类型
     */
    List<BehaviorType> selectAllBehaviorTypes();
    /**
     * 查询所有行为类型（包括禁用的）
     */
    List<BehaviorType> selectAllBehaviorTypesIncludeDisabled();
}