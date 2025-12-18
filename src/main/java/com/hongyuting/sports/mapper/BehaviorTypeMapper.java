package com.hongyuting.sports.mapper;

import com.hongyuting.sports.entity.BehaviorType;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BehaviorTypeMapper {
    int insertBehaviorType(BehaviorType behaviorType);
    int updateBehaviorType(BehaviorType behaviorType);
    int disableBehaviorType(@Param("typeId") int typeId);
    BehaviorType selectBehaviorTypeById(@Param("typeId") int typeId);
    BehaviorType selectBehaviorTypeByName(@Param("typeName") String typeName);
    List<BehaviorType> selectAllBehaviorTypes();
    List<BehaviorType> selectAllBehaviorTypesIncludeDisabled();
}
