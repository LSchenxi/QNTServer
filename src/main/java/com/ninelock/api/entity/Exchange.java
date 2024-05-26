package com.ninelock.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 交易所表
 * 
 * @author 
 * @version 创建时间: 2024-04-30 13:10:29
 */
@Setter
@Getter
@TableName("exchange")
@Accessors(chain = true)
public class Exchange {

    /**
     * 交易所表自增主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 交易所名称
     */
    @TableField(value = "exchange")
    private String exchange;

    /**
     * 交易所名称(对前台用户显示用)
     */
    @TableField(value = "exchange_name_web")
    private String exchangeNameWeb;

    /**
     * 是否删除（0：删除；1：存在）
     */
    @TableLogic
    @TableField(value = "del_flag", fill = FieldFill.INSERT)
    private Integer delFlag;
}
