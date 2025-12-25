package com.hongyuting.sports.mapper;

import com.hongyuting.sports.entity.BehaviorType;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 行为类型映射接口
 */
public interface BehaviorTypeMapper extends BaseMapper<BehaviorType, Integer> {

    /**
     * 根据名称查询行为类型
     */
    BehaviorType selectByName(@Param("typeName") String typeName);

    /**
     * 禁用行为类型
     */
    int disable(@Param("typeId") Integer typeId);

    /**
     * 查询所有行为类型
     */
    List<BehaviorType> selectAllIncludeDisabled();
}