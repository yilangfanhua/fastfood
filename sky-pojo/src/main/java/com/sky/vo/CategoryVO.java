package com.sky.vo;

import com.sky.entity.Category;
import java.time.LocalDateTime;

public record CategoryVO(Long id, Integer type, String name, Integer sort, Integer status, LocalDateTime createTime, LocalDateTime updateTime, Long createUser, Long updateUser) {
    public static CategoryVO from(Category entity) {
        return new CategoryVO(entity.getId(), entity.getType(), entity.getName(), entity.getSort(), entity.getStatus(), entity.getCreateTime(), entity.getUpdateTime(), entity.getCreateUser(), entity.getUpdateUser());
    }
}
