package com.ninelock.api.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ninelock.api.core.auth.Session;
import com.ninelock.api.entity.Strategy;
import com.ninelock.api.mapper.StrategyMapper;
import com.ninelock.api.request.DeleteReq;
import com.ninelock.api.request.StrategyCreateReq;
import com.ninelock.api.request.StrategyReq;
import com.ninelock.api.request.StrategyUpdateReq;
import com.ninelock.api.response.Result;
import com.ninelock.api.response.StrategyPageResp;
import com.ninelock.api.response.StrategyResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;

import static com.ninelock.api.core.auth.Session.SESSION_ID;
import static com.ninelock.api.core.constant.EntityConstant.IS_EXIST;

@Slf4j
@Service
public class StrategyService extends ServiceImpl<StrategyMapper, Strategy> {

    public Result<?> getPage(StrategyReq strategyReq) {
        final Session session = (Session) StpUtil.getSession().get(SESSION_ID);
        Long userId = session.getUserId();

        final int page = strategyReq.getPage();
        final int size = strategyReq.getSize();

        // 查询分页列表
        final LambdaQueryWrapper<Strategy> wrapper = new LambdaQueryWrapper<>();
        if(null!=strategyReq.getName() && !"".equals(strategyReq.getName())){
            wrapper.like(Strategy::getName, strategyReq.getName());
        }
        wrapper.eq(Strategy::getDelFlag, IS_EXIST);
        wrapper.eq(Strategy::getCreateId, userId);
        final Page<Strategy> strategyPage = this.page(new Page<>(page, size), wrapper);
        // 转为响应对象
        final List<StrategyResp> strategyRespList = strategyPage.getRecords().stream().map(record -> {
            final StrategyResp strategyResp = new StrategyResp();
            BeanUtils.copyProperties(record, strategyResp);
            // 到期时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if(null != record.getExpirationTime()){
                strategyResp.setExpirationTimeStr(sdf.format(record.getExpirationTime()));
            } else {
                strategyResp.setExpirationTimeStr("");
            }
            // 创建时间
            if(null != record.getCreateTime()){
                strategyResp.setCreateTimeStr(sdf.format(record.getCreateTime()));
            } else {
                strategyResp.setCreateTimeStr("");
            }
            return strategyResp;
        }).toList();
        final StrategyPageResp strategyPageResp = new StrategyPageResp();
        strategyPageResp.setTotal(strategyPage.getTotal());
        strategyPageResp.setRecords(strategyRespList);
        return Result.ok(strategyPageResp);
    }

    public Result<?> getStrategy(Long id) {
        return Result.ok(this.baseMapper.selectById(id));
    }

    public Result<?> createStrategy(StrategyCreateReq req) {
        final Session session = (Session) StpUtil.getSession().get(SESSION_ID);
        Long userId = session.getUserId();
        final LambdaQueryWrapper<Strategy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Strategy::getName, req.getName());
        wrapper.eq(Strategy::getDelFlag, IS_EXIST);
        wrapper.eq(Strategy::getCreateId, userId);
        long count = this.count(wrapper);
        if(count > 0){
            return Result.error("策略名称重复", null);
        }
        Strategy strategy = new Strategy();
        BeanUtils.copyProperties(req, strategy);
        strategy.setCreateId(userId);
        if(this.save(strategy)){
            return Result.ok();
        }else {
            return Result.error("保存失败", null);
        }
    }

    public Result<?> updateStrategy(StrategyUpdateReq req) {
        final LambdaQueryWrapper<Strategy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Strategy::getName, req.getName());
        wrapper.eq(Strategy::getDelFlag, IS_EXIST);
        wrapper.ne(Strategy::getId, req.getId());
        long count = this.count(wrapper);
        if(count > 0){
            return Result.error("策略名称重复", null);
        }
        Strategy strategy = new Strategy();
        BeanUtils.copyProperties(req, strategy);
        if (this.updateById(strategy)) {
            return Result.ok();
        } else {
            return Result.error("更新失败", null);
        }
    }

    public Result<?> deleteStrategy(DeleteReq req) {
        if (this.removeById(req.getId())) {
            return Result.ok();
        } else {
            return Result.error("删除失败", null);
        }
    }
}
