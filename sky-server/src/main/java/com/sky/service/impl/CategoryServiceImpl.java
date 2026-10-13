package com.sky.service.impl;

import com.sky.dto.*;
import com.sky.entity.Category;
import com.sky.exception.BusinessException;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import com.sky.vo.CategoryVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryMapper mapper;
    public CategoryServiceImpl(CategoryMapper mapper) { this.mapper=mapper; }
    @Override public Long create(CategoryCreateDTO dto) {
        Category category = new Category(); category.setType(dto.type()); category.setName(dto.name());
        category.setSort(dto.sort()); category.setStatus(0); mapper.insert(category); return category.getId();
    }
    @Override public CategoryVO getById(Long id) { return CategoryVO.from(require(id)); }
    @Override @Transactional(readOnly=true)
    public PageResult<CategoryVO> page(CategoryPageQueryDTO dto) {
        int offset=dto.offset();
        return new PageResult<>(mapper.count(dto.getName(),dto.getType()),
                mapper.page(dto.getName(),dto.getType(),offset,dto.getPageSize()).stream().map(CategoryVO::from).toList());
    }
    @Override public List<CategoryVO> list(Integer type) { return mapper.enabledList(type).stream().map(CategoryVO::from).toList(); }
    @Override public void update(CategoryUpdateDTO dto) {
        require(dto.id()); Category category=new Category(); category.setId(dto.id()); category.setType(dto.type());
        category.setName(dto.name()); category.setSort(dto.sort());
        if (mapper.update(category)==0) throw new BusinessException(404,"分类不存在");
    }
    @Override public void delete(Long id) {
        if (mapper.deleteById(id)==0) throw new BusinessException(404,"分类不存在");
    }
    @Override public void changeStatus(Long id,int status) {
        if (status!=0 && status!=1) throw new BusinessException(400,"状态无效");
        require(id); Category category=new Category(); category.setId(id); category.setStatus(status);
        if (mapper.updateStatus(category)==0) throw new BusinessException(404,"分类不存在");
    }
    private Category require(Long id) {
        Category category=mapper.findById(id);
        if (category==null) throw new BusinessException(404,"分类不存在");
        return category;
    }
}
