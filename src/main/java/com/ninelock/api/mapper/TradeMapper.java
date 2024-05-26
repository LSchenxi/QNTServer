package com.ninelock.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ninelock.api.entity.Trade;
import org.apache.ibatis.annotations.Mapper;

/**
 * 交易表
 *
 * @author 
 * @version 创建时间: 2024-04-30 17:29:37
 */
@Mapper
public interface TradeMapper extends BaseMapper<Trade> {
}
