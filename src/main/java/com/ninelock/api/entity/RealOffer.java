package com.ninelock.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 实盘表
 * 
 * @author 
 * @version 创建时间: 2024-05-13 11:46:27
 */
@Setter
@Getter
@TableName("real_offer")
@Accessors(chain = true)
public class RealOffer {

    /**
     * 实盘表主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 实盘名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 实盘状态（运行中；已停止）
     */
    @TableField(value = "status")
    private String status;

    /**
     * 实盘盈利
     */
    @TableField(value = "profit")
    private Double profit;

    /**
     * 挂载服务器id
     */
    @TableField(value = "server_id")
    private Long serverId;

    /**
     * 实盘端口
     */
    @TableField(value = "real_offer_port")
    private String realOfferPort;

    /**
     * 执行策略id
     */
    @TableField(value = "strategy_id")
    private Long strategyId;

    /**
     * 公开状态1：公开 0：不公开
     */
    @TableField(value = "open_flag")
    private Integer openFlag;

    /**
     * K线周期
     */
    @TableField(value = "k_line_period")
    private Integer kLinePeriod;

    /**
     * K线周期单位（秒，分钟，小时，天）
     */
    @TableField(value = "k_line_unit")
    private String kLineUnit;

    /**
     * 实例ID
     */
    @TableField(value = "app_id")
    private String appId;

    /**
     * 自动交易开关（0：关 1：开）
     */
    @TableField(value = "auto_trading")
    private Integer autoTrading;

    /**
     * 基础仓位价值
     */
    @TableField(value = "basic_warehouse_value")
    private String basicWarehouseValue;

    /**
     * 最大持仓个数
     */
    @TableField(value = "max_holding_number")
    private String maxHoldingNumber;

    /**
     * 换币保护期(H)
     */
    @TableField(value = "change_symbol_protect_period")
    private String changeSymbolProtectPeriod;

    /**
     * 换币观察期(H)
     */
    @TableField(value = "change_symbol_watch_period")
    private String changeSymbolWatchPeriod;

    /**
     * 换币观察最大个数
     */
    @TableField(value = "max_change_symbol_watch_number")
    private String maxChangeSymbolWatchNumber;

    /**
     * 换币沉默期(H)
     */
    @TableField(value = "change_symbol_silent_period")
    private String changeSymbolSilentPeriod;

    /**
     * 换币最小月化
     */
    @TableField(value = "change_symbol_min_monthly")
    private String changeSymbolMinMonthly;

    /**
     * 选币过滤(暴涨暴跌)开关（0：关 1：开）
     */
    @TableField(value = "symbol_filter")
    private Integer symbolFilter;

    /**
     * 乖离率均线周期(4H 8H 1D)
     */
    @TableField(value = "bias_average_period")
    private String biasAveragePeriod;

    /**
     * 最小乖离率
     */
    @TableField(value = "min_bias")
    private String minBias;

    /**
     * 最大乖离率
     */
    @TableField(value = "max_bias")
    private String maxBias;

    /**
     * 自动加仓开关（0：关 1：开）
     */
    @TableField(value = "auto_add_warehouse")
    private Integer autoAddWarehouse;

    /**
     * 加仓比例
     */
    @TableField(value = "add_ratio")
    private String addRatio;

    /**
     * 加仓最大倍数
     */
    @TableField(value = "max_add_multiple")
    private String maxAddMultiple;

    /**
     * 自动减仓开关（0：关 1：开）
     */
    @TableField(value = "auto_reduce_warehouse")
    private Integer autoReduceWarehouse;

    /**
     * 减仓比例
     */
    @TableField(value = "reduce_ratio")
    private String reduceRatio;

    /**
     * 清仓保护开关（0：关 1：开）
     */
    @TableField(value = "clear_warehouse_protect")
    private Integer clearWarehouseProtect;

    /**
     * 最大清仓保护个数
     */
    @TableField(value = "max_clear_protect_number")
    private String maxClearProtectNumber;

    /**
     * 最小持仓盈亏率
     */
    @TableField(value = "min_holding_phase_rate")
    private String minHoldingPhaseRate;

    /**
     * 最大持仓数包含清仓保护数开关（0：关 1：开）
     */
    @TableField(value = "max_holding_contain_clear_protect")
    private Integer maxHoldingContainClearProtect;

    /**
     * 初始资产(U)
     */
    @TableField(value = "initial_asset")
    private String initialAsset;

    /**
     * 提币资产(U)
     */
    @TableField(value = "extract_asset")
    private String extractAsset;

    /**
     * 单笔最小交易额
     */
    @TableField(value = "min_transaction_one")
    private String minTransactionOne;

    /**
     * 单笔最大交易额
     */
    @TableField(value = "max_transaction_one")
    private String maxTransactionOne;

    /**
     * 套利溢价
     */
    @TableField(value = "arbitrage_premium")
    private String arbitragePremium;

    /**
     * 初始均衡溢价
     */
    @TableField(value = "initial_equilibrium_premium")
    private String initialEquilibriumPremium;

    /**
     * 最大均衡亏损溢价
     */
    @TableField(value = "max_equilibrium_loss_premium")
    private String maxEquilibriumLossPremium;

    /**
     * 挂单开关（0：关 1：开）
     */
    @TableField(value = "order_flag")
    private Integer orderFlag;

    /**
     * 单笔最大挂单量(U)
     */
    @TableField(value = "max_pending_order")
    private String maxPendingOrder;

    /**
     * 挂单距离
     */
    @TableField(value = "order_distance")
    private String orderDistance;

    /**
     * 过滤同名币最大差价
     */
    @TableField(value = "max_diff_same_symbol_filter")
    private String maxDiffSameSymbolFilter;

    /**
     * 购买平台抵扣币(U)
     */
    @TableField(value = "platform_deduction_coin")
    private String platformDeductionCoin;

    /**
     * 创建人
     */
    @TableField(value = "create_id", fill = FieldFill.INSERT)
    private Long createId;

    /**
     * 创建人名称
     */
    @TableField(value = "creator", fill = FieldFill.INSERT)
    private String creator;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date createTime;

    /**
     * 更新时间（系统时间）
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date updateTime;

    /**
     * 更新人
     */
    @TableField(value = "update_id", fill = FieldFill.INSERT_UPDATE)
    private Long updateId;

    /**
     * 是否删除（0：删除；1：存在）
     */
    @TableLogic
    @TableField(value = "del_flag", fill = FieldFill.INSERT)
    private Integer delFlag;
}
