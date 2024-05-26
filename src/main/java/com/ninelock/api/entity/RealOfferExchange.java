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
 * @version 创建时间: 2024-05-23 15:59:46
 */
@Setter
@Getter
@TableName("real_offer_exchange")
@Accessors(chain = true)
public class RealOfferExchange {

    /**
     * 实盘交易所详情表自增主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 实盘id
     */
    @TableField(value = "real_offer_id")
    private Long realOfferId;

    /**
     * 交易所详细表id
     */
    @TableField(value = "exchange_id")
    private Long exchangeId;

    /**
     * 交易所索引名称
     */
    @TableField(value = "exchange")
    private String exchange;

    /**
     * 资产
     */
    @TableField(value = "asset")
    private String asset;

    /**
     * USDT
     */
    @TableField(value = "usdt")
    private String usdt;

    /**
     * 日交易量(U)
     */
    @TableField(value = "daily_trading_volume_usdt")
    private String dailyTradingVolumeUsdt;

    /**
     * 日交易量(挂|吃)
     */
    @TableField(value = "daily_trading_volume")
    private String dailyTradingVolume;

    /**
     * 交易对数
     */
    @TableField(value = "transaction_pairs")
    private String transactionPairs;

    /**
     * 交易量占比%
     */
    @TableField(value = "transaction_volume_proportion")
    private String transactionVolumeProportion;

    /**
     * 交易量占比基数
     */
    @TableField(value = "transaction_volume_proportion_count")
    private Long transactionVolumeProportionCount;

    /**
     * 滑点%
     */
    @TableField(value = "slippage")
    private String slippage;

    /**
     * 滑点计算基数
     */
    @TableField(value = "slippage_count")
    private Long slippageCount;

    /**
     * 成功率%
     */
    @TableField(value = "success_rate")
    private String successRate;

    /**
     * 成功率计算基数
     */
    @TableField(value = "success_rate_count")
    private Long successRateCount;

    /**
     * 增量溢价
     */
    @TableField(value = "incremental_premium")
    private String incrementalPremium;

    /**
     * 交易所状态（0：停用 1：启用）
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 交易所停用时是否清仓（0：不清仓 1：清仓）
     */
    @TableField(value = "clear_wraehouse")
    private Integer clearWraehouse;

    /**
     * 清仓模式（1：吃单对冲 2：挂单对冲 3：吃单卖出 4：挂单卖出）
     */
    @TableField(value = "clear_mode")
    private Integer clearMode;

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
