package com.ninelock.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AddCurrencyToRealOfferReq {
    @NotNull(message = "实盘信息错误")
    private Long id;
    private List<Long> currencyListValue;
}
