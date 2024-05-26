package com.ninelock.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 服务器表
 * 
 * @author 
 * @version 创建时间: 2024-04-16 16:59:58
 */
@Setter
@Getter
@TableName("server")
@Accessors(chain = true)
public class Server {

    /**
     * 服务器表主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 服务器IP
     */
    @TableField(value = "server_ip")
    private String serverIp;

    /**
     * 操作系统
     */
    @TableField(value = "operating_system")
    private String operatingSystem;

    /**
     * 实盘数量
     */
    @TableField(value = "real_offer_number")
    private Integer realOfferNumber;

    /**
     * 协议
     */
    @TableField(value = "protocol")
    private String protocol;

    /**
     * 版本
     */
    @TableField(value = "version")
    private String version;

    /**
     * 状态
     */
    @TableField(value = "status")
    private String status;

    /**
     * 地理位置
     */
    @TableField(value = "position")
    private String position;

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
