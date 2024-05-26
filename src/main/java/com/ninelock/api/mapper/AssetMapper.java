package com.ninelock.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ninelock.api.entity.Asset;
import org.apache.ibatis.annotations.Mapper;

/**
 * USDT价值信息表
 *
 * @author 
 * @version 创建时间: 2024-05-26 10:29:52
 */
@Mapper
public interface AssetMapper extends BaseMapper<Asset> {
}
