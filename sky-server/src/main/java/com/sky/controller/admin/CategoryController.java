package com.sky.controller.admin;

import com.sky.dto.*;
import com.sky.result.*;
import com.sky.service.CategoryService;
import com.sky.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/category")
@Tag(name="分类管理")
@Validated
public class CategoryController {
    private final CategoryService service;
    public CategoryController(CategoryService service) { this.service=service; }
    @PostMapping @Operation(summary="新增分类")
    public Result<Long> create(@Valid @RequestBody CategoryCreateDTO dto) { return Result.success(service.create(dto)); }
    @GetMapping("/page") @Operation(summary="分类分页查询")
    public Result<PageResult<CategoryVO>> page(@Valid @ModelAttribute CategoryPageQueryDTO dto) { return Result.success(service.page(dto)); }
    @GetMapping("/list") @Operation(summary="查询启用分类列表")
    public Result<List<CategoryVO>> list(@RequestParam(required=false) @Min(1) @Max(2) Integer type) { return Result.success(service.list(type)); }
    @GetMapping("/{id}") @Operation(summary="分类详情")
    public Result<CategoryVO> get(@PathVariable @Positive Long id) { return Result.success(service.getById(id)); }
    @PutMapping @Operation(summary="修改分类")
    public Result<Void> update(@Valid @RequestBody CategoryUpdateDTO dto) { service.update(dto); return Result.success(); }
    @DeleteMapping("/{id}") @Operation(summary="删除分类")
    public Result<Void> delete(@PathVariable @Positive Long id) { service.delete(id); return Result.success(); }
    @PostMapping("/status/{status}") @Operation(summary="启用或禁用分类")
    public Result<Void> status(@PathVariable @Min(0) @Max(1) int status,@RequestParam @Positive Long id) {
        service.changeStatus(id,status); return Result.success();
    }
}
