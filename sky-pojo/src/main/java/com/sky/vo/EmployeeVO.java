package com.sky.vo;

import com.sky.entity.Employee;
import java.time.LocalDateTime;

public record EmployeeVO(Long id, String username, String name, String phone, String sex, String idNumber, Integer status, LocalDateTime createTime, LocalDateTime updateTime, Long createUser, Long updateUser) {
    public static EmployeeVO from(Employee entity) {
        return new EmployeeVO(entity.getId(), entity.getUsername(), entity.getName(), entity.getPhone(), entity.getSex(), entity.getIdNumber(), entity.getStatus(), entity.getCreateTime(), entity.getUpdateTime(), entity.getCreateUser(), entity.getUpdateUser());
    }
}
