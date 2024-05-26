package com.ninelock.api.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ninelock.api.core.auth.Session;
import com.ninelock.api.entity.Asset;
import com.ninelock.api.entity.RealOffer;
import com.ninelock.api.entity.Strategy;
import com.ninelock.api.entity.Trade;
import com.ninelock.api.mapper.AssetMapper;
import com.ninelock.api.mapper.RealOfferMapper;
import com.ninelock.api.mapper.TradeMapper;
import com.ninelock.api.request.RealOfferRevenueChartDataReq;
import com.ninelock.api.response.RealOfferListResp;
import com.ninelock.api.response.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.ninelock.api.core.auth.Session.SESSION_ID;

@Slf4j
@Service
public class DataOverviewService {
    @Resource
    private RealOfferMapper realOfferMapper;
    @Resource
    private TradeMapper tradeMapper;
    @Resource
    private AssetMapper assetMapper;

    public Result<?> getRealOfferList() {
        final Session session = (Session) StpUtil.getSession().get(SESSION_ID);
        Long userId = session.getUserId();
        final LambdaQueryWrapper<RealOffer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RealOffer::getCreateId, userId);
        List<RealOfferListResp> realOfferListResps = realOfferMapper.selectList(wrapper).stream().map(record -> {
            RealOfferListResp realOfferListResp = new RealOfferListResp();
            realOfferListResp.setId(record.getId());
            realOfferListResp.setName(record.getName());
            return realOfferListResp;
        }).toList();
        return Result.ok(realOfferListResps);
    }

    public Result<?> getAccountProfitChartData(RealOfferRevenueChartDataReq req) {
        final Session session = (Session) StpUtil.getSession().get(SESSION_ID);
        Long userId = session.getUserId();
        final LambdaQueryWrapper<RealOffer> realOfferWrapper = new LambdaQueryWrapper<>();
        realOfferWrapper.eq(RealOffer::getCreateId, userId);
        List<String> appIdList = realOfferMapper.selectList(realOfferWrapper).stream().map(RealOffer::getAppId).toList();
        if(CollectionUtils.isEmpty(appIdList)){
            return Result.ok();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        final LambdaQueryWrapper<Trade> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Trade::getAppId, appIdList);
        if (!"2000-0-1 0:0:0".equals(req.getCurrentTime())) {
            String currentTime = req.getCurrentTime();
            DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(currentTime, formatter);
            Date date = Date.from(zonedDateTime.toInstant());
            wrapper.gt(Trade::getCreateTime, date);
        }
        wrapper.orderByAsc(Trade::getCreateTime);
        result = tradeMapper.selectList(wrapper).stream().map(record -> {
            Map<String, Object> dataMap = new HashMap<>();
            List<Object> dataList = new ArrayList<>();
            dataList.add(record.getCreateTime());
            dataList.add(record.getAllProfit());
            dataMap.put("value", dataList);
            return dataMap;
        }).toList();
        return Result.ok(result);
    }

    public Result<?> getAccountStrategyChartData(RealOfferRevenueChartDataReq req) {
        List<Map<String, Object>> result = new ArrayList<>();
        final LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Asset::getRealId, -1L);
        if (!"2000-0-1 0:0:0".equals(req.getCurrentTime())) {
            String currentTime = req.getCurrentTime();
            DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(currentTime, formatter);
            Date date = Date.from(zonedDateTime.toInstant());
            wrapper.gt(Asset::getRecordTime, date);
        }
        wrapper.orderByAsc(Asset::getRecordTime);
        result = assetMapper.selectList(wrapper).stream().map(record -> {
            Map<String, Object> dataMap = new HashMap<>();
            List<Object> dataList = new ArrayList<>();
            dataList.add(record.getRecordTime());
            dataList.add(record.getValue());
            dataMap.put("value", dataList);
            return dataMap;
        }).toList();
        return Result.ok(result);
    }
}
