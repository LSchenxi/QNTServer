package com.ninelock.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 交易所详情表
 * 
 * @author 
 * @version 创建时间: 2024-04-30 13:10:16
 */
@Setter
@Getter
@TableName("exchange_detail")
@Accessors(chain = true)
public class ExchangeDetail {

    /**
     * 交易所详细表自增主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 协议id
     */
    @TableField(value = "protocol_id")
    private Long protocolId;

    /**
     * 交易所id
     */
    @TableField(value = "exchange_id")
    private Long exchangeId;

    /**
     * 账户access key
     */
    @TableField(value = "access_key")
    private String accessKey;

    /**
     * 账户secret key
     */
    @TableField(value = "secret_key")
    private String secretKey;

    /**
     * 账户passphrase
     */
    @TableField(value = "passphrase")
    private String passphrase;

    /**
     * v5标志0：关 1：开
     */
    @TableField(value = "v5_flag")
    private Integer v5Flag;

    /**
     * v3标志0：关 1：开
     */
    @TableField(value = "v3_flag")
    private Integer v3Flag;

    /**
     * 版本号
     */
    @TableField(value = "version")
    private String version;

    /**
     * 密码
     */
    @TableField(value = "password")
    private String password;

    /**
     * 交易所标签
     */
    @TableField(value = "exchange_label")
    private String exchangeLabel;

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
