package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CategoryMapper {
    @AutoFill(OperationType.INSERT) int insert(Category category);
    @AutoFill(OperationType.UPDATE) int update(Category category);
    @AutoFill(OperationType.UPDATE) int updateStatus(Category category);
    Category findById(@Param("id") Long id);
    int deleteById(@Param("id") Long id);
    long count(@Param("name") String name, @Param("type") Integer type);
    List<Category> page(@Param("name") String name, @Param("type") Integer type, @Param("offset") int offset, @Param("limit") int limit);
    List<Category> enabledList(@Param("type") Integer type);
}
