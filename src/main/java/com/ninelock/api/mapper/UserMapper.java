package com.ninelock.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ninelock.api.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表
 *
 * @author 
 * @version 创建时间: 2024-04-09 08:53:24
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
