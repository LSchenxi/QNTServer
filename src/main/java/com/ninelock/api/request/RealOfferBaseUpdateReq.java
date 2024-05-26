package com.ninelock.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RealOfferBaseUpdateReq {
    @NotNull(message = "实盘ID不能为空")
    private Long id;
    @NotNull(message = "实盘名称不能为空")
    private String name;
    @NotNull(message = "执行策略不能为空")
    private Long strategyId;
    @NotNull(message = "k线周期不能为空")
    private Integer klinePeriod;
    @NotNull(message = "k线周期单位不能为空")
    private String klineUnit;
}
