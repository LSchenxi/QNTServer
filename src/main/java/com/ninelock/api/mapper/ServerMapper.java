package com.ninelock.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ninelock.api.entity.Server;
import org.apache.ibatis.annotations.Mapper;

/**
 * 服务器表
 *
 * @author 
 * @version 创建时间: 2024-04-16 14:58:38
 */
@Mapper
public interface ServerMapper extends BaseMapper<Server> {
}
