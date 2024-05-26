package com.ninelock.api.response;

import lombok.Data;

import java.util.List;

/**
 * @author gongym
 * @version 创建时间: 2023-12-18 17:35
 */
@Data
public class PageResp {
    private Long total;
    private List<?> records;

    public PageResp() {
        this.total = 0L;
        this.records = List.of();
    }
}
