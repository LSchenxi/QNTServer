package com.ninelock.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 定时任务锚点表
 * 
 * @author 
 * @version 创建时间: 2024-05-22 18:24:46
 */
@Setter
@Getter
@TableName("timed_task_anchor_point")
@Accessors(chain = true)
public class TimedTaskAnchorPoint {

    /**
     * 定时任务锚点表自增主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 上次计算real_offer_symbol表信息时查询到的最后一条trade表id
     */
    @TableField(value = "real_offer_symbol_trade_id")
    private Long realOfferSymbolTradeId;

    /**
     * 上次计算real_offer_exchange表信息时查询到的最后一条trade表id
     */
    @TableField(value = "real_offer_exchange_trade_id")
    private Long realOfferExchangeTradeId;

    /**
     * 上次计算real_offer_symbol_exchange表信息时查询到的最后一条trade表id
     */
    @TableField(value = "real_offer_symbol_exchange_trade_id")
    private Long realOfferSymbolExchangeTradeId;

    /**
     * 是否删除（0：删除；1：存在）
     */
    @TableLogic
    @TableField(value = "del_flag", fill = FieldFill.INSERT)
    private Integer delFlag;
}
