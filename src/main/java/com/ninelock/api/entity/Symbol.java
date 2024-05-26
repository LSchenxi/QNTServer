package com.ninelock.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 币对信息表
 * 
 * @author 
 * @version 创建时间: 2024-05-20 15:33:49
 */
@Setter
@Getter
@TableName("symbol")
@Accessors(chain = true)
public class Symbol {

    /**
     * 自增主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 货币对名称（系统名称）
     */
    @TableField(value = "name")
    private String name;

    /**
     * mexc交易所
     */
    @TableField(value = "ex_mexc")
    private Integer exMexc;

    /**
     * mexc交易所价格精度
     */
    @TableField(value = "ex_mexc_price")
    private Integer exMexcPrice;

    /**
     * mexc交易所数量精度
     */
    @TableField(value = "ex_mexc_amount")
    private Integer exMexcAmount;

    /**
     * gate交易所
     */
    @TableField(value = "ex_gate")
    private Integer exGate;

    /**
     * gate交易所价格精度
     */
    @TableField(value = "ex_gate_price")
    private Integer exGatePrice;

    /**
     * gate交易所数量精度
     */
    @TableField(value = "ex_gate_amount")
    private Integer exGateAmount;

    /**
     * kucoin交易所
     */
    @TableField(value = "ex_kucoin")
    private Integer exKucoin;

    /**
     * kucoin交易所价格精度
     */
    @TableField(value = "ex_kucoin_price")
    private Integer exKucoinPrice;

    /**
     * kucoin交易所数量精度
     */
    @TableField(value = "ex_kucoin_amount")
    private Integer exKucoinAmount;

    /**
     * htx交易所
     */
    @TableField(value = "ex_htx")
    private Integer exHtx;

    /**
     * htx交易所价格精度
     */
    @TableField(value = "ex_htx_price")
    private Integer exHtxPrice;

    /**
     * htx交易所数量精度
     */
    @TableField(value = "ex_htx_amount")
    private Integer exHtxAmount;

    /**
     * bitget交易所
     */
    @TableField(value = "ex_bitget")
    private Integer exBitget;

    /**
     * bitget交易所价格精度
     */
    @TableField(value = "ex_bitget_price")
    private Integer exBitgetPrice;

    /**
     * bitget交易所数量精度
     */
    @TableField(value = "ex_bitget_amount")
    private Integer exBitgetAmount;

    /**
     * okx交易所
     */
    @TableField(value = "ex_okx")
    private Integer exOkx;

    /**
     * okx交易所价格精度
     */
    @TableField(value = "ex_okx_price")
    private Integer exOkxPrice;

    /**
     * okx交易所数量精度
     */
    @TableField(value = "ex_okx_amount")
    private Integer exOkxAmount;

    /**
     * bybit交易所
     */
    @TableField(value = "ex_bybit")
    private Integer exBybit;

    /**
     * bybit交易所价格精度
     */
    @TableField(value = "ex_bybit_price")
    private Integer exBybitPrice;

    /**
     * bybit交易所数量精度
     */
    @TableField(value = "ex_bybit_amount")
    private Integer exBybitAmount;

    /**
     * coinex交易所
     */
    @TableField(value = "ex_coinex")
    private Integer exCoinex;

    /**
     * coinex交易所价格精度
     */
    @TableField(value = "ex_coinex_price")
    private Integer exCoinexPrice;

    /**
     * coinex交易所数量精度
     */
    @TableField(value = "ex_coinex_amount")
    private Integer exCoinexAmount;

    /**
     * bitmart交易所
     */
    @TableField(value = "ex_bitmart")
    private Integer exBitmart;

    /**
     * bitmart交易所价格精度
     */
    @TableField(value = "ex_bitmart_price")
    private Integer exBitmartPrice;

    /**
     * bitmart交易所数量精度
     */
    @TableField(value = "ex_bitmart_amount")
    private Integer exBitmartAmount;

    /**
     * 价格精度
     */
    @TableField(value = "price_precision")
    private Integer pricePrecision;

    /**
     * 数量精度
     */
    @TableField(value = "amount_precision")
    private Integer amountPrecision;

    /**
     * 买入费率
     */
    @TableField(value = "buy_commission")
    private String buyCommission;

    /**
     * 卖出费率
     */
    @TableField(value = "sell_commission")
    private String sellCommission;

    /**
     * 是否删除（1：存在；0：删除）
     */
    @TableLogic
    @TableField(value = "del_flag", fill = FieldFill.INSERT)
    private Integer delFlag;
}
