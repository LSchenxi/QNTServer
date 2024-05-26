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
 * @version 创建时间: 2024-04-30 20:36:22
 */
@Setter
@Getter
@TableName("trade_order")
@Accessors(chain = true)
public class TradeOrder {

    /**
     * 自增主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 交易Id
     */
    @TableField(value = "trade_id")
    private String tradeId;

    /**
     * 订单Id
     */
    @TableField(value = "order_id")
    private String orderId;

    /**
     * 对冲的原始订单Id
     */
    @TableField(value = "origin_order_id")
    private String originOrderId;

    /**
     * 方向（buy，sell）
     */
    @TableField(value = "side")
    private String side;

    /**
     * 交易类型（原始订单，对冲订单）
     */
    @TableField(value = "style")
    private String style;

    /**
     * 交易所
     */
    @TableField(value = "exchange")
    private String exchange;

    /**
     * 行情时间戳
     */
    @TableField(value = "depth_ts")
    private Long depthTs;

    /**
     * 接收行情时间戳
     */
    @TableField(value = "depth_receive_ts")
    private Long depthReceiveTs;

    /**
     * 下单时间
     */
    @TableField(value = "open_ts")
    private Long openTs;

    /**
     * 下单价格
     */
    @TableField(value = "open_price")
    private String openPrice;

    /**
     * 下单数量
     */
    @TableField(value = "open_amount")
    private String openAmount;

    /**
     * 成交时间
     */
    @TableField(value = "close_ts")
    private Long closeTs;

    /**
     * 成交价格
     */
    @TableField(value = "close_price")
    private String closePrice;

    /**
     * 成交数量
     */
    @TableField(value = "close_amount")
    private String closeAmount;

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
