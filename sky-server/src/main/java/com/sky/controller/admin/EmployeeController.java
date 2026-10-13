package com.sky.controller.admin;

import com.sky.dto.EmployeeLoginDTO;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.sky.dto.*;
import com.sky.vo.EmployeeVO;
import com.sky.result.PageResult;
import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/admin/employee")
@Tag(name = "员工管理")
@Validated
public class EmployeeController {
    private final EmployeeService service;
    public EmployeeController(EmployeeService service) { this.service = service; }

    @PostMapping("/login")
    @Operation(summary = "员工登录", security = {})
    @SecurityRequirements
    public Result<EmployeeLoginVO> login(@Valid @RequestBody EmployeeLoginDTO dto) {
        return Result.success(service.login(dto));
    }

    @PostMapping
    @Operation(summary="新增员工")
    public Result<Long> create(@Valid @RequestBody EmployeeCreateDTO dto) { return Result.success(service.create(dto)); }

    @GetMapping("/page")
    @Operation(summary="员工分页查询")
    public Result<PageResult<EmployeeVO>> page(@Valid @ModelAttribute EmployeePageQueryDTO dto) { return Result.success(service.page(dto)); }

    @GetMapping("/{id}")
    @Operation(summary="查询员工详情")
    public Result<EmployeeVO> get(@PathVariable @Positive Long id) { return Result.success(service.getById(id)); }

    @PutMapping
    @Operation(summary="修改员工")
    public Result<Void> update(@Valid @RequestBody EmployeeUpdateDTO dto) { service.update(dto); return Result.success(); }

    @DeleteMapping("/{id}")
    @Operation(summary="删除员工")
    public Result<Void> delete(@PathVariable @Positive Long id) { service.delete(id); return Result.success(); }

    @PostMapping("/status/{status}")
    @Operation(summary="启用或禁用员工")
    public Result<Void> status(@PathVariable @Min(0) @Max(1) int status, @RequestParam @Positive Long id) {
        service.changeStatus(id,status); return Result.success();
    }
}
