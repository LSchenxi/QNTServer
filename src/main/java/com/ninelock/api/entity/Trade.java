package com.ninelock.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 交易表
 * 
 * @author 
 * @version 创建时间: 2024-04-30 17:29:37
 */
@Setter
@Getter
@TableName("trade")
@Accessors(chain = true)
public class Trade {

    /**
     * 自增主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 实例Id
     */
    @TableField(value = "app_id")
    private String appId;

    /**
     * 交易Id
     */
    @TableField(value = "trade_id")
    private String tradeId;

    /**
     * 对冲的原始交易Id
     */
    @TableField(value = "origin_trade_id")
    private String originTradeId;

    /**
     * 模式（balance，arbitrage，hedge）
     */
    @TableField(value = "mode")
    private String mode;

    /**
     * 货币对
     */
    @TableField(value = "symbol")
    private String symbol;

    /**
     * 利润
     */
    @TableField(value = "profit")
    private String profit;

    /**
     * 总利润
     */
    @TableField(value = "all_profit")
    private String allProfit;

    /**
     * 实例总利润
     */
    @TableField(value = "app_profit")
    private String appProfit;

    /**
     * 货币对总利润
     */
    @TableField(value = "symbol_profit")
    private String symbolProfit;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date updateTime;
}
