package com.sky.mapper;

import com.sky.entity.Employee;
import com.sky.annotation.AutoFill;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface EmployeeMapper {
    Employee findByUsername(@Param("username") String username);
    Employee findById(@Param("id") Long id);
    Employee findBootstrapAdmin();
    int insertBootstrap(Employee employee);
    @AutoFill(OperationType.INSERT)
    int insert(Employee employee);
    @AutoFill(OperationType.UPDATE)
    int update(Employee employee);
    @AutoFill(OperationType.UPDATE)
    int updateStatus(Employee employee);
    long count(@Param("name") String name);
    List<Employee> page(@Param("name") String name, @Param("offset") int offset, @Param("limit") int limit);
    int deleteById(@Param("id") Long id);
}
