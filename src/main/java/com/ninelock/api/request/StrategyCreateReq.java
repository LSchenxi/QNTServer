package com.ninelock.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StrategyCreateReq {
    @NotNull(message = "策略名称不能为空")
    private String name;
    @NotNull(message = "到期时间不能为空")
    private java.util.Date expirationTime;
}
