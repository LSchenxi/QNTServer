package com.ninelock.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ninelock.api.entity.Exchange;
import org.apache.ibatis.annotations.Mapper;

/**
 * 交易所表
 *
 * @author 
 * @version 创建时间: 2024-04-30 13:10:29
 */
@Mapper
public interface ExchangeMapper extends BaseMapper<Exchange> {
}
