package com.sky.dto;

import jakarta.validation.constraints.*;

public class CategoryPageQueryDTO {
    @Min(1) private int page = 1;
    @Min(1) @Max(100) private int pageSize = 10;
    @Size(max=64)
    private String name;
    @Min(1) @Max(2)
    private Integer type;
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public int offset() { return Math.multiplyExact(page - 1, pageSize); }
}
