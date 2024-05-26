package com.ninelock.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * USDT价值信息表
 * 
 * @author 
 * @version 创建时间: 2024-05-26 10:29:52
 */
@Setter
@Getter
@TableName("asset")
@Accessors(chain = true)
public class Asset {

    /**
     * 自增主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 实盘Id
     */
    @TableField(value = "real_id")
    private Long realId;

    /**
     * 总USDT价值
     */
    @TableField(value = "value")
    private String value;

    /**
     * mexc交易所USDT价值
     */
    @TableField(value = "value_mexc")
    private String valueMexc;

    /**
     * gate交易所USDT价值
     */
    @TableField(value = "value_gate")
    private String valueGate;

    /**
     * kucoin交易所USDT价值
     */
    @TableField(value = "value_kucoin")
    private String valueKucoin;

    /**
     * htx交易所USDT价值
     */
    @TableField(value = "value_htx")
    private String valueHtx;

    /**
     * bitget交易所USDT价值
     */
    @TableField(value = "value_bitget")
    private String valueBitget;

    /**
     * okx交易所USDT价值
     */
    @TableField(value = "value_okx")
    private String valueOkx;

    /**
     * bybit交易所USDT价值
     */
    @TableField(value = "value_bybit")
    private String valueBybit;

    /**
     * coinex交易所USDT价值
     */
    @TableField(value = "value_coinex")
    private String valueCoinex;

    /**
     * bitmart交易所USDT价值
     */
    @TableField(value = "value_bitmart")
    private String valueBitmart;

    /**
     * 记录时间
     */
    @TableField(value = "record_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date recordTime;

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
