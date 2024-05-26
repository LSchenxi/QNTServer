package com.ninelock.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ninelock.api.entity.Symbol;
import org.apache.ibatis.annotations.Mapper;

/**
 * 币信息表
 *
 * @author 
 * @version 创建时间: 2024-04-30 14:48:07
 */
@Mapper
public interface SymbolMapper extends BaseMapper<Symbol> {
}
