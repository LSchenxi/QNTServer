package com.ninelock.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ninelock.api.entity.TimedTaskAnchorPoint;
import org.apache.ibatis.annotations.Mapper;

/**
 * 定时任务锚点表
 *
 * @author 
 * @version 创建时间: 2024-05-22 18:24:46
 */
@Mapper
public interface TimedTaskAnchorPointMapper extends BaseMapper<TimedTaskAnchorPoint> {
}
