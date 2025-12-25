package com.hongyuting.sports.mapper;

import java.util.List;

/**
 * 通用Mapper接口
 */
public interface BaseMapper<T, ID> {

    /**
     * 插入记录
     */
    int insert(T entity);

    /**
     * 根据ID删除记录
     */
    int deleteById(ID id);

    /**
     * 根据ID更新记录
     */
    int updateById(T entity);

    /**
     * 根据ID查询记录
     */
    T selectById(ID id);

    /**
     * 查询所有记录
     */
    List<T> selectAll();

    /**
     * 统计记录总数
     */
    Integer selectTotalCount();
}
