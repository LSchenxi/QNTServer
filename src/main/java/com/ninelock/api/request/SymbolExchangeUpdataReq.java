package com.ninelock.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SymbolExchangeUpdataReq {
    @NotNull(message = "当前币信息错误")
    private Long currentSymboleId;
    @NotNull(message = "实盘信息错误")
    private Long realOfferId;
    private List<Long> currentSymbolExchangeData;
}
