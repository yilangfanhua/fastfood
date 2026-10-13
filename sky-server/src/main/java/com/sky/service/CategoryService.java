package com.sky.service;

import com.sky.dto.*;
import com.sky.vo.CategoryVO;
import com.sky.result.PageResult;
import java.util.List;

public interface CategoryService {
    Long create(CategoryCreateDTO dto);
    CategoryVO getById(Long id);
    PageResult<CategoryVO> page(CategoryPageQueryDTO dto);
    List<CategoryVO> list(Integer type);
    void update(CategoryUpdateDTO dto);
    void delete(Long id);
    void changeStatus(Long id,int status);
}
