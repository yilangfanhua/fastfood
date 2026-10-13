package com.sky.vo;

public record EmployeeLoginVO(Long id, String username, String name, String token) {
    @Override public String toString() { return "EmployeeLoginVO[id="+id+", username="+username+", token=[REDACTED]]"; }
}
