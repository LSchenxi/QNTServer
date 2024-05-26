package com.ninelock.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 实盘币-交易所信息表
 * 
 * @author 
 * @version 创建时间: 2024-05-22 19:32:20
 */
@Setter
@Getter
@TableName("real_offer_symbol_exchange")
@Accessors(chain = true)
public class RealOfferSymbolExchange {

    /**
     * 实盘中的币-交易所信息表主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 实盘币详情表主键
     */
    @TableField(value = "real_offer_symbol_id")
    private Long realOfferSymbolId;

    /**
     * 交易所名称
     */
    @TableField(value = "exchange")
    private String exchange;

    /**
     * 费率
     */
    @TableField(value = "rate")
    private String rate;

    /**
     * 延迟
     */
    @TableField(value = "delay")
    private String delay;

    /**
     * 延迟计算基数
     */
    @TableField(value = "delay_count")
    private Long delayCount;

    /**
     * 当前余额
     */
    @TableField(value = "current_balance")
    private String currentBalance;

    /**
     * 当前币数
     */
    @TableField(value = "current_symbol_number")
    private String currentSymbolNumber;

    /**
     * 币值
     */
    @TableField(value = "symbol_value")
    private String symbolValue;

    /**
     * 卖一
     */
    @TableField(value = "sell_one")
    private String sellOne;

    /**
     * 买一
     */
    @TableField(value = "buy_one")
    private String buyOne;

    /**
     * 市场深度
     */
    @TableField(value = "market_depth")
    private String marketDepth;

    /**
     * 总成交额
     */
    @TableField(value = "total_transaction_amount")
    private String totalTransactionAmount;

    /**
     * 滑点
     */
    @TableField(value = "sliding_point")
    private String slidingPoint;

    /**
     * 滑点计算基数
     */
    @TableField(value = "sliding_point_count")
    private Long slidingPointCount;

    /**
     * 成功率
     */
    @TableField(value = "success_rate")
    private String successRate;

    /**
     * 成功率计算基数
     */
    @TableField(value = "success_rate_count")
    private Long successRateCount;

    /**
     * 状态（0：禁用 1：启用）
     */
    @TableField(value = "status")
    private Integer status;

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
