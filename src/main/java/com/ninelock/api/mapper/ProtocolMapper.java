package com.ninelock.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ninelock.api.entity.Protocol;
import org.apache.ibatis.annotations.Mapper;

/**
 * 协议类型表
 *
 * @author 
 * @version 创建时间: 2024-04-30 13:14:59
 */
@Mapper
public interface ProtocolMapper extends BaseMapper<Protocol> {
}
