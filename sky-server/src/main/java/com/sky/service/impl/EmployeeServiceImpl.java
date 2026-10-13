package com.sky.service.impl;

import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.exception.BusinessException;
import com.sky.mapper.EmployeeMapper;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import com.sky.dto.*;
import com.sky.vo.EmployeeVO;
import com.sky.result.PageResult;
import com.sky.context.BaseContext;
import java.util.Objects;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeMapper mapper;
    private final PasswordEncoder passwords;
    private final JwtUtil jwt;

    public EmployeeServiceImpl(EmployeeMapper mapper, PasswordEncoder passwords, JwtUtil jwt) {
        this.mapper = mapper; this.passwords = passwords; this.jwt = jwt;
    }

    @Override
    public EmployeeLoginVO login(EmployeeLoginDTO dto) {
        Employee employee = mapper.findByUsername(dto.username());
        if (employee == null || dto.password().getBytes(StandardCharsets.UTF_8).length > 72
                || !passwords.matches(dto.password(), employee.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (employee.getStatus() != 1) throw new BusinessException(403, "账户已禁用");
        return new EmployeeLoginVO(employee.getId(), employee.getUsername(), employee.getName(), jwt.createToken(employee.getId()));
    }

    @Override
    public Long create(EmployeeCreateDTO dto) {
        if ("admin".equalsIgnoreCase(dto.username())) throw new BusinessException(409, "admin 为保留用户名");
        if (dto.password().getBytes(StandardCharsets.UTF_8).length > 72) throw new BusinessException(400, "密码不得超过72个UTF-8字节");
        Employee employee = new Employee();
        employee.setUsername(dto.username()); employee.setName(dto.name()); employee.setPhone(dto.phone());
        employee.setSex(dto.sex()); employee.setIdNumber(dto.idNumber());
        employee.setPassword(passwords.encode(dto.password())); employee.setStatus(1);
        mapper.insert(employee); return employee.getId();
    }

    @Override public EmployeeVO getById(Long id) { return EmployeeVO.from(require(id)); }

    @Override
    @Transactional(readOnly=true)
    public PageResult<EmployeeVO> page(EmployeePageQueryDTO dto) {
        int offset = dto.offset();
        return new PageResult<>(mapper.count(dto.getName()), mapper.page(dto.getName(),offset,dto.getPageSize())
                .stream().map(EmployeeVO::from).toList());
    }

    @Override
    public void update(EmployeeUpdateDTO dto) {
        Employee existing = require(dto.id());
        if (Boolean.TRUE.equals(existing.getBootstrapAdmin()) && !existing.getUsername().equals(dto.username())) {
            throw new BusinessException(403, "初始化管理员用户名不可修改");
        }
        if (!Boolean.TRUE.equals(existing.getBootstrapAdmin()) && "admin".equalsIgnoreCase(dto.username())) {
            throw new BusinessException(409, "admin 为保留用户名");
        }
        Employee employee = new Employee(); employee.setId(dto.id());
        employee.setUsername(dto.username()); employee.setName(dto.name()); employee.setPhone(dto.phone());
        employee.setSex(dto.sex()); employee.setIdNumber(dto.idNumber());
        if (mapper.update(employee) == 0) throw new BusinessException(404, "员工不存在");
    }

    @Override
    public void delete(Long id) {
        protect(require(id));
        if (mapper.deleteById(id) == 0) throw new BusinessException(404, "员工不存在");
    }

    @Override
    public void changeStatus(Long id, int status) {
        if (status != 0 && status != 1) throw new BusinessException(400, "状态无效");
        Employee existing = require(id);
        if (status == 0) protect(existing);
        Employee employee = new Employee(); employee.setId(id); employee.setStatus(status);
        if (mapper.updateStatus(employee) == 0) throw new BusinessException(404, "员工不存在");
    }

    private Employee require(Long id) {
        Employee employee = mapper.findById(id);
        if (employee == null) throw new BusinessException(404, "员工不存在");
        return employee;
    }

    private void protect(Employee employee) {
        if (Boolean.TRUE.equals(employee.getBootstrapAdmin()) || Objects.equals(employee.getId(),BaseContext.getCurrentId())) {
            throw new BusinessException(403, "不能删除或停用当前账户及初始化管理员");
        }
    }
}
