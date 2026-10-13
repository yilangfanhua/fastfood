package com.sky.service;

import com.sky.dto.EmployeeLoginDTO;
import com.sky.vo.EmployeeLoginVO;
import com.sky.dto.*;
import com.sky.vo.EmployeeVO;
import com.sky.result.PageResult;

public interface EmployeeService {
    EmployeeLoginVO login(EmployeeLoginDTO dto);
    Long create(EmployeeCreateDTO dto);
    EmployeeVO getById(Long id);
    PageResult<EmployeeVO> page(EmployeePageQueryDTO dto);
    void update(EmployeeUpdateDTO dto);
    void delete(Long id);
    void changeStatus(Long id, int status);
}
