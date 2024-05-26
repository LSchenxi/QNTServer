package com.ninelock.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ninelock.api.entity.TradeOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 交易表
 *
 * @author 
 * @version 创建时间: 2024-04-30 20:36:22
 */
@Mapper
public interface TradeOrderMapper extends BaseMapper<TradeOrder> {
}
