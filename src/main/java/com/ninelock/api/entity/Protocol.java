package com.ninelock.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 协议类型表
 * 
 * @author 
 * @version 创建时间: 2024-04-30 13:14:59
 */
@Setter
@Getter
@TableName("protocol")
@Accessors(chain = true)
public class Protocol {

    /**
     * 协议表自增主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 协议类型
     */
    @TableField(value = "protocol_type")
    private String protocolType;

    /**
     * 是否删除（0：删除；1：存在）
     */
    @TableLogic
    @TableField(value = "del_flag", fill = FieldFill.INSERT)
    private Integer delFlag;
}
