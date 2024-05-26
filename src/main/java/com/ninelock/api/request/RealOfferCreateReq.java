package com.ninelock.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RealOfferCreateReq {
    @NotNull(message = "实盘名称不能为空")
    private String name;
    @NotNull(message = "挂载服务器不能为空")
    private Long serverId;
    @NotNull(message = "实盘端口不能为空")
    private String realOfferPort;
    @NotNull(message = "执行策略不能为空")
    private Long strategyId;
    @NotNull(message = "k线周期不能为空")
    private Integer linePeriod;
    @NotNull(message = "k线周期单位不能为空")
    private String lineUnit;
    @NotNull(message = "实盘程序索引不能为空")
    private String appId;
    @NotNull(message = "交易设置不能为空")
    private List<Map<String, Long>> exchangeMapList;
}
