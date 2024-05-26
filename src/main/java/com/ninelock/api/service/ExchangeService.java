package com.ninelock.api.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ninelock.api.core.auth.Session;
import com.ninelock.api.entity.*;
import com.ninelock.api.mapper.ExchangeDetailMapper;
import com.ninelock.api.mapper.ExchangeMapper;
import com.ninelock.api.mapper.ProtocolMapper;
import com.ninelock.api.request.DeleteReq;
import com.ninelock.api.request.ExchangeDetailCreateReq;
import com.ninelock.api.request.ExchangeDetailReq;
import com.ninelock.api.request.ExchangeDetailUpdateReq;
import com.ninelock.api.response.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.ninelock.api.core.auth.Session.SESSION_ID;
import static com.ninelock.api.core.constant.EntityConstant.IS_EXIST;

@Slf4j
@Service
public class ExchangeService extends ServiceImpl<ExchangeDetailMapper, ExchangeDetail> {

    @Resource
    private ProtocolMapper protocolMapper;
    @Resource
    private ExchangeMapper exchangeMapper;

    public Result<?> getPage(ExchangeDetailReq req) {
        final Session session = (Session) StpUtil.getSession().get(SESSION_ID);
        Long userId = session.getUserId();
        final int page = req.getPage();
        final int size = req.getSize();

        // 查询分页列表
        final LambdaQueryWrapper<ExchangeDetail> wrapper = new LambdaQueryWrapper<>();
        if (null != req.getName() && !"".equals(req.getName())) {
            wrapper.like(ExchangeDetail::getExchangeLabel, req.getName());
        }
        wrapper.eq(ExchangeDetail::getDelFlag, IS_EXIST);
        wrapper.eq(ExchangeDetail::getCreateId, userId);
        final Page<ExchangeDetail> exchangeDetailPage = this.page(new Page<>(page, size), wrapper);
        // 转为响应对象
        final List<ExchangeDetailResp> exchangeDetailRespList = exchangeDetailPage.getRecords().stream().map(record -> {
            final ExchangeDetailResp exchangeDetailResp = new ExchangeDetailResp();
            BeanUtils.copyProperties(record, exchangeDetailResp);
            exchangeDetailResp.setExchangeName(exchangeMapper.selectById(record.getExchangeId()).getExchangeNameWeb());
            return exchangeDetailResp;
        }).toList();
        final ExchangeDetailPageResp exchangeDetailPageResp = new ExchangeDetailPageResp();
        exchangeDetailPageResp.setTotal(exchangeDetailPage.getTotal());
        exchangeDetailPageResp.setRecords(exchangeDetailRespList);
        return Result.ok(exchangeDetailPageResp);
    }

    public Result<?> getExchangeDetail(Long id) {
        return Result.ok(this.baseMapper.selectById(id));
    }

    public Result<?> createExchangeDetail(ExchangeDetailCreateReq req) {
        final Session session = (Session) StpUtil.getSession().get(SESSION_ID);
        Long userId = session.getUserId();
        final LambdaQueryWrapper<ExchangeDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExchangeDetail::getExchangeLabel, req.getExchangeLabel());
        wrapper.eq(ExchangeDetail::getDelFlag, IS_EXIST);
        wrapper.eq(ExchangeDetail::getCreateId, userId);
        long count = this.count(wrapper);
        if(count > 0){
            return Result.error("交易所标签/名称重复", null);
        }
        ExchangeDetail exchangeDetail = new ExchangeDetail();
        BeanUtils.copyProperties(req, exchangeDetail);
        exchangeDetail.setCreateId(userId);
        if(this.save(exchangeDetail)){
            return Result.ok();
        }else {
            return Result.error("保存失败", null);
        }
    }

    public Result<?> updateExchangeDetail(ExchangeDetailUpdateReq req) {
        final LambdaQueryWrapper<ExchangeDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExchangeDetail::getExchangeLabel, req.getExchangeLabel());
        wrapper.eq(ExchangeDetail::getDelFlag, IS_EXIST);
        wrapper.ne(ExchangeDetail::getId, req.getId());
        long count = this.count(wrapper);
        if(count > 0){
            return Result.error("交易所标签/名称重复", null);
        }
        ExchangeDetail exchangeDetail = new ExchangeDetail();
        BeanUtils.copyProperties(req, exchangeDetail);
        if (this.updateById(exchangeDetail)) {
            return Result.ok();
        } else {
            return Result.error("更新失败", null);
        }
    }

    public Result<?> deleteExchange(DeleteReq req) {
        if (this.removeById(req.getId())) {
            return Result.ok();
        } else {
            return Result.error("删除失败", null);
        }
    }

    public Result<?> getProtocol() {
        final LambdaQueryWrapper<Protocol> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Protocol::getDelFlag, IS_EXIST);
        List<Protocol> protocols = protocolMapper.selectList(wrapper);
        if(CollectionUtils.isNotEmpty(protocols)){
            List<ProtocolSelectResp> protocolSelectResps = protocols.stream().map(record -> {
                ProtocolSelectResp protocolSelectResp = new ProtocolSelectResp();
                protocolSelectResp.setValue(record.getId());
                protocolSelectResp.setLabel(record.getProtocolType());
                return protocolSelectResp;
            }).toList();
            return Result.ok(protocolSelectResps);
        }
        return Result.error("暂无数据", null);
    }

    public Result<?> getExchange() {
        final LambdaQueryWrapper<Exchange> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Exchange::getDelFlag, IS_EXIST);
        List<Exchange> exchangeList = exchangeMapper.selectList(wrapper);
        if(CollectionUtils.isNotEmpty(exchangeList)){
            List<ExchangeSelectResp> exchangeSelectRespList = exchangeList.stream().map(record -> {
                ExchangeSelectResp exchangeSelectResp = new ExchangeSelectResp();
                exchangeSelectResp.setValue(record.getId());
                exchangeSelectResp.setLabel(record.getExchangeNameWeb());
                return exchangeSelectResp;
            }).toList();
            return Result.ok(exchangeSelectRespList);
        }
        return Result.error("暂无数据", null);
    }
}
