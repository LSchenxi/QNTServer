package com.ninelock.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RealOfferBaseAutoTradeUpdateReq {
    @NotNull(message = "实盘ID不能为空")
    private Long id;
    @NotNull(message = "自动交易开关不能为空")
    private Integer autoTrading;
    @NotNull(message = "基础仓位价值不能为空")
    private String basicWarehouseValue;
    @NotNull(message = "最大持仓个数不能为空")
    private String maxHoldingNumber;
    @NotNull(message = "换币保护期(H)不能为空")
    private String changeSymbolProtectPeriod;
    @NotNull(message = "换币观察期(H)不能为空")
    private String changeSymbolWatchPeriod;
    @NotNull(message = "换币观察最大个数不能为空")
    private String maxChangeSymbolWatchNumber;
    @NotNull(message = "换币沉默期(H)不能为空")
    private String changeSymbolSilentPeriod;
    @NotNull(message = "换币最小月化不能为空")
    private String changeSymbolMinMonthly;
    @NotNull(message = "选币过滤不能为空")
    private Integer symbolFilter;
    @NotNull(message = "乖离率均线周期不能为空")
    private String biasAveragePeriod;
    @NotNull(message = "最小乖离率不能为空")
    private String minBias;
    @NotNull(message = "最大乖离率不能为空")
    private String maxBias;
    @NotNull(message = "自动加仓不能为空")
    private Integer autoAddWarehouse;
    @NotNull(message = "加仓比例不能为空")
    private String addRatio;
    @NotNull(message = "加仓最大倍数不能为空")
    private String maxAddMultiple;
    @NotNull(message = "自动减仓不能为空")
    private Integer autoReduceWarehouse;
    @NotNull(message = "减仓比例不能为空")
    private String reduceRatio;
    @NotNull(message = "清仓保护不能为空")
    private Integer clearWarehouseProtect;
    @NotNull(message = "最大清仓保护个数不能为空")
    private String maxClearProtectNumber;
    @NotNull(message = "最小持仓盈亏率不能为空")
    private String minHoldingPhaseRate;
    @NotNull(message = "最大持仓包含清仓保护不能为空")
    private Integer maxHoldingContainClearProtect;
}
