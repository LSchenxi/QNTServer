package com.ninelock.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 实盘交易所信息表
 * 
 * @author 
 * @version 创建时间: 2024-05-23 19:33:21
 */
@Setter
@Getter
@TableName("real_offer_symbol")
@Accessors(chain = true)
public class RealOfferSymbol {

    /**
     * 实盘币详情表自增主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 实盘id
     */
    @TableField(value = "real_offer_id")
    private Long realOfferId;

    /**
     * 币id
     */
    @TableField(value = "symbol_id")
    private Long symbolId;

    /**
     * 币名称
     */
    @TableField(value = "symbol_name")
    private String symbolName;

    /**
     * 币状态（1：启用 2：清仓 3：锁定 4：暂停 5：待初始化 6：初始化完成）
     */
    @TableField(value = "symbol_status")
    private Integer symbolStatus;

    /**
     * 摊薄成本(U)
     */
    @TableField(value = "diluted_costs")
    private String dilutedCosts;

    /**
     * 买入成本(U)
     */
    @TableField(value = "buy_cost")
    private String buyCost;

    /**
     * 持仓价值(U)
     */
    @TableField(value = "holding_value")
    private String holdingValue;

    /**
     * 价值偏差(U)
     */
    @TableField(value = "value_deviation")
    private String valueDeviation;

    /**
     * 价值偏差基数
     */
    @TableField(value = "value_deviation_count")
    private Long valueDeviationCount;

    /**
     * 持仓盈亏(U)
     */
    @TableField(value = "holding_phase")
    private String holdingPhase;

    /**
     * 套利盈亏(U)
     */
    @TableField(value = "arbitrage_phase")
    private String arbitragePhase;

    /**
     * 1h盈亏(U)
     */
    @TableField(value = "one_hour_phase")
    private String oneHourPhase;

    /**
     * 1h月化(%)
     */
    @TableField(value = "one_hour_monthly")
    private String oneHourMonthly;

    /**
     * 4h盈亏(U)
     */
    @TableField(value = "four_hour_phase")
    private String fourHourPhase;

    /**
     * 4h月化(%)
     */
    @TableField(value = "four_hour_monthly")
    private String fourHourMonthly;

    /**
     * 24h盈亏(U)
     */
    @TableField(value = "one_day_phase")
    private String oneDayPhase;

    /**
     * 24h月化(%)
     */
    @TableField(value = "one_day_monthly")
    private String oneDayMonthly;

    /**
     * 3D盈亏(U)
     */
    @TableField(value = "three_day_phase")
    private String threeDayPhase;

    /**
     * 3D月化(%)
     */
    @TableField(value = "three_day_monthly")
    private String threeDayMonthly;

    /**
     * 启用时长(D)
     */
    @TableField(value = "enable_duration")
    private String enableDuration;

    /**
     * 均衡溢价
     */
    @TableField(value = "equilibrium_premium")
    private String equilibriumPremium;

    /**
     * 交易溢价
     */
    @TableField(value = "transaction_premium")
    private String transactionPremium;

    /**
     * 挂单大小
     */
    @TableField(value = "order_size")
    private String orderSize;

    /**
     * 挂单距离
     */
    @TableField(value = "order_distance")
    private String orderDistance;

    /**
     * 延迟（整数）
     */
    @TableField(value = "delay_time")
    private String delayTime;

    /**
     * 耗时（整数）
     */
    @TableField(value = "consuming_time")
    private String consumingTime;

    /**
     * 轮询（整数）
     */
    @TableField(value = "polling_time")
    private String pollingTime;

    /**
     * 延迟基数
     */
    @TableField(value = "delay_count")
    private Long delayCount;

    /**
     * 交易中
     */
    @TableField(value = "in_transaction")
    private String inTransaction;

    /**
     * 交易对个数
     */
    @TableField(value = "transaction_pairs")
    private String transactionPairs;

    /**
     * 初始均衡溢价
     */
    @TableField(value = "initial_equilibrium_premium")
    private String initialEquilibriumPremium;

    /**
     * 套利溢价
     */
    @TableField(value = "arbitrage_premium")
    private String arbitragePremium;

    /**
     * 单笔最大挂单量(U)
     */
    @TableField(value = "max_pending_order")
    private String maxPendingOrder;

    /**
     * 建仓时间
     */
    @TableField(value = "create_warehouse_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date createWarehouseTime;

    /**
     * 建仓价格
     */
    @TableField(value = "create_warehouse_price")
    private String createWarehousePrice;

    /**
     * 平仓价格
     */
    @TableField(value = "closing_warehouse_price")
    private String closingWarehousePrice;

    /**
     * 买入均价
     */
    @TableField(value = "buy_average_price")
    private String buyAveragePrice;

    /**
     * 买入价值(U)
     */
    @TableField(value = "buy_price")
    private String buyPrice;

    /**
     * 卖出均价
     */
    @TableField(value = "sell_average_price")
    private String sellAveragePrice;

    /**
     * 卖出价值(U)
     */
    @TableField(value = "sell_price")
    private String sellPrice;

    /**
     * 交易次数
     */
    @TableField(value = "transactions_count")
    private String transactionsCount;

    /**
     * 启停等操作时间
     */
    @TableField(value = "operation_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date operationTime;

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
