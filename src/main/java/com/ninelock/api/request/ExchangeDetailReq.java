package com.ninelock.api.request;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExchangeDetailReq {
    /**
     * 页码
     */
    @NotNull(message = "页码不能为空")
    private Integer page;

    /**
     * 每页数量
     */
    @NotNull(message = "每页数量不能为空")
    private Integer size;

    /**
     * 交易所名称
     */
    private String name;
}
