package com.ninelock.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RealOfferTradeParamUpdateReq {
    @NotNull(message = "实盘ID不能为空")
    private Long id;
    @NotNull(message = "初始资产(U)不能为空")
    private String initialAsset;
    @NotNull(message = "提币资产(U)不能为空")
    private String extractAsset;
    @NotNull(message = "单笔最小交易额不能为空")
    private String minTransactionOne;
    @NotNull(message = "单笔最大交易额不能为空")
    private String maxTransactionOne;
    @NotNull(message = "套利溢价不能为空")
    private String arbitragePremium;
    @NotNull(message = "初始均衡溢价不能为空")
    private String initialEquilibriumPremium;
    @NotNull(message = "最大均衡亏损溢价不能为空")
    private String maxEquilibriumLossPremium;
    @NotNull(message = "挂单开关不能为空")
    private Integer orderFlag;
    @NotNull(message = "单笔最大挂单量(U)不能为空")
    private String maxPendingOrder;
    @NotNull(message = "挂单距离不能为空")
    private String orderDistance;
    @NotNull(message = "过滤同名币最大差价不能为空")
    private String maxDiffSameSymbolFilter;
    @NotNull(message = "购买平台抵扣币(U)不能为空")
    private String platformDeductionCoin;
}
