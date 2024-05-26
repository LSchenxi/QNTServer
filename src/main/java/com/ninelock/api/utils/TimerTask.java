package com.ninelock.api.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ninelock.api.entity.*;
import com.ninelock.api.mapper.*;
import com.ninelock.api.response.QntRobotResponse;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static com.ninelock.api.core.constant.EntityConstant.IS_EXIST;

@Component
public class TimerTask {

    @Resource
    private RealOfferMapper realOfferMapper;
    @Resource
    private ServerMapper serverMapper;
    @Resource
    private TimedTaskAnchorPointMapper timedTaskAnchorPointMapper;
    @Resource
    private TradeMapper tradeMapper;
    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private RealOfferSymbolMapper realOfferSymbolMapper;
    @Resource
    private RealOfferSymbolExchangeMapper realOfferSymbolExchangeMapper;
    @Resource
    private RealOfferExchangeMapper realOfferExchangeMapper;

    //第一次运行于服务启动完毕后1分钟 然后每2分钟运行一次
//    @Scheduled(fixedRate = 2 * 60 * 1000, initialDelay = 60 * 1000)
    public void getRealOfferHealth() {
        // 获取所有实盘列表
        final LambdaQueryWrapper<RealOffer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RealOffer::getDelFlag, IS_EXIST);
        List<RealOffer> realOfferList = realOfferMapper.selectList(wrapper);
        for (RealOffer realOffer : realOfferList) {
            String reqUrl = "http://" + serverMapper.selectById(realOffer.getServerId()).getServerIp() + ":" + realOffer.getRealOfferPort() + "/api/v1/health";
            HttpResponse response = HttpRequest.get(reqUrl).timeout(10 * 1000).execute();
            System.out.println(response.body());
            if (response.isOk()) {
                // 实盘运行正常
                String result = response.body();
                QntRobotResponse qntRobotResponse = JSONUtil.toBean(result, QntRobotResponse.class);
                System.out.println(qntRobotResponse.getSuccess() + ":" + result);
                if (qntRobotResponse.getSuccess()) {
                    // 实盘运行正常
                    realOffer.setStatus("运行中");
                } else {
                    // 实盘运行异常
                    realOffer.setStatus("实盘异常");
                }
            } else {
                // 实盘停止运行
                realOffer.setStatus("已停止");
            }
            realOfferMapper.updateById(realOffer);
        }
    }

    @Scheduled(fixedRate = 2 * 60 * 1000, initialDelay = 10 * 1000)
    public void setRealOfferSymbolExchangeInfo() {
        System.out.println("==========================开始定时计算币-交易所数据================================");
        System.out.println(new Date());
        // 计算汇总
        Map<String, RealOfferSymbol> symbolResult = new HashMap<>();
        Map<String, RealOfferExchange> exchangeResult = new HashMap<>();
        Map<String, RealOfferSymbolExchange> symbolExchangeResult = new HashMap<>();
        Map<Long, Long> realOfferTradeOrderTotal = new HashMap<>();
        final LambdaQueryWrapper<RealOfferExchange> realOfferExchangeWrapper1 = new LambdaQueryWrapper<>();
        realOfferExchangeWrapper1.eq(RealOfferExchange::getDelFlag, IS_EXIST);
        List<RealOfferExchange> realOfferExchanges = realOfferExchangeMapper.selectList(realOfferExchangeWrapper1);
        for(RealOfferExchange realOfferExchange : realOfferExchanges){
            Long oldRealOfferExchangeCount = realOfferExchange.getTransactionVolumeProportionCount();
            if(null == oldRealOfferExchangeCount){
                oldRealOfferExchangeCount = 0L;
            }
            if(null != realOfferTradeOrderTotal.get(realOfferExchange.getRealOfferId())){
                oldRealOfferExchangeCount = oldRealOfferExchangeCount + realOfferTradeOrderTotal.get(realOfferExchange.getRealOfferId());
            }
            realOfferTradeOrderTotal.put(realOfferExchange.getRealOfferId(), oldRealOfferExchangeCount);
        }
        // 获取上次计算锚点
        final LambdaQueryWrapper<TimedTaskAnchorPoint> timedTaskAnchorPointWrapper = new LambdaQueryWrapper<>();
        timedTaskAnchorPointWrapper.eq(TimedTaskAnchorPoint::getDelFlag, IS_EXIST);
        List<TimedTaskAnchorPoint> timedTaskAnchorPoints = timedTaskAnchorPointMapper.selectList(timedTaskAnchorPointWrapper);
        TimedTaskAnchorPoint timedTaskAnchorPoint = timedTaskAnchorPoints.get(0);
        final LambdaQueryWrapper<Trade> tradeWrapper = new LambdaQueryWrapper<>();
        // 增量获取交易数据
        tradeWrapper.gt(Trade::getId, timedTaskAnchorPoint.getRealOfferSymbolExchangeTradeId());
        List<Trade> tradeList = tradeMapper.selectList(tradeWrapper);
        if (CollectionUtils.isNotEmpty(tradeList)) {
            // 开始计算
            for (Trade trade : tradeList) {
                final LambdaQueryWrapper<RealOffer> realOfferWrapper = new LambdaQueryWrapper<>();
                realOfferWrapper.eq(RealOffer::getAppId, trade.getAppId());
                List<RealOffer> realOfferList = realOfferMapper.selectList(realOfferWrapper);
                if (CollectionUtils.isNotEmpty(realOfferList)) {
                    RealOffer realOffer = realOfferList.get(0);
                    String symbolName = trade.getSymbol().split("-")[0];
                    // 定位要添加的RealOfferSymbol表数据
                    final LambdaQueryWrapper<RealOfferSymbol> realOfferSymbolWrapper = new LambdaQueryWrapper<>();
                    realOfferSymbolWrapper.eq(RealOfferSymbol::getRealOfferId, realOffer.getId());
                    realOfferSymbolWrapper.eq(RealOfferSymbol::getSymbolName, symbolName);
                    List<RealOfferSymbol> realOfferSymbolList = realOfferSymbolMapper.selectList(realOfferSymbolWrapper);
                    if (CollectionUtils.isNotEmpty(realOfferSymbolList)) {
                        RealOfferSymbol realOfferSymbol = new RealOfferSymbol();
                        if (symbolResult.containsKey(realOffer.getId() + "_" + symbolName)) {
                            realOfferSymbol = symbolResult.get(realOffer.getId() + "_" + symbolName);
                        } else {
                            realOfferSymbol = realOfferSymbolList.get(0);
                            symbolResult.put(realOffer.getId() + "_" + symbolName, realOfferSymbol);
                        }
                        final LambdaQueryWrapper<TradeOrder> tradeOrderWrapper = new LambdaQueryWrapper<>();
                        tradeOrderWrapper.eq(TradeOrder::getTradeId, trade.getTradeId());
                        List<TradeOrder> tradeOrderList = tradeOrderMapper.selectList(tradeOrderWrapper);
                        if (CollectionUtils.isNotEmpty(tradeOrderList)) {
                            // 价值偏差 // 买入-卖出
                            String oldValueDeviationStr = realOfferSymbol.getValueDeviation();
                            Long oldValueDeviationCount = realOfferSymbol.getValueDeviationCount();
                            BigDecimal oldValueDeviation = new BigDecimal(0);
                            BigDecimal currentValueDeviation = new BigDecimal(0);
                            BigDecimal valueDeviation = new BigDecimal(0);
                            if(null != oldValueDeviationStr && !"".equals(oldValueDeviationStr)){
                                oldValueDeviation = BigDecimal.valueOf(Double.parseDouble(oldValueDeviationStr.split("\\|")[0]));
                                valueDeviation = oldValueDeviation.multiply(BigDecimal.valueOf(oldValueDeviationCount));
                                oldValueDeviationCount = oldValueDeviationCount + 1;
                            } else {
                                oldValueDeviationCount = 1L;
                            }
                            realOfferSymbol.setValueDeviationCount(oldValueDeviationCount);
                            String exchangeName = "";
                            if (trade.getMode().equals("hedge")) {
                                exchangeName = null != tradeOrderList.get(0).getExchange() ? tradeOrderList.get(0).getExchange() : tradeOrderList.get(1).getExchange();
                            } else {
                                TradeOrder buyOrder = new TradeOrder();
                                TradeOrder sellOrder = new TradeOrder();
                                BigDecimal buyPrice = new BigDecimal(0);
                                BigDecimal buyAmount = new BigDecimal(0);
                                BigDecimal sellPrice = new BigDecimal(0);
                                BigDecimal sellAmount = new BigDecimal(0);
                                if("buy".equals(tradeOrderList.get(0).getSide())){
                                    buyOrder = tradeOrderList.get(0);
                                    sellOrder = tradeOrderList.get(1);
                                } else {
                                    buyOrder = tradeOrderList.get(1);
                                    sellOrder = tradeOrderList.get(0);
                                }
                                if(null != buyOrder.getClosePrice()){
                                    buyPrice = BigDecimal.valueOf(Double.parseDouble(buyOrder.getClosePrice()));
                                }
                                if(null != buyOrder.getCloseAmount()){
                                    buyAmount = BigDecimal.valueOf(Double.parseDouble(buyOrder.getCloseAmount()));
                                }
                                if(null != sellOrder.getClosePrice()){
                                    sellPrice = BigDecimal.valueOf(Double.parseDouble(sellOrder.getClosePrice()));
                                }
                                if(null != sellOrder.getCloseAmount()){
                                    sellAmount = BigDecimal.valueOf(Double.parseDouble(sellOrder.getCloseAmount()));
                                }
                                currentValueDeviation = buyPrice.multiply(buyAmount).subtract(sellPrice.multiply(sellAmount));
                            }
                            valueDeviation = valueDeviation.add(currentValueDeviation);
                            valueDeviation = valueDeviation.divide(BigDecimal.valueOf(oldValueDeviationCount),2,RoundingMode.HALF_UP);
                            realOfferSymbol.setValueDeviation(valueDeviation+"|"+"0");
                            for (TradeOrder tradeOrder : tradeOrderList) {
                                if ("".equals(exchangeName)) {
                                    exchangeName = tradeOrder.getExchange();
                                }
                                if(null != realOfferSymbol.getId()){
                                    // 延时
                                    long delay1 = 0L;
                                    long delay2 = 0L;
                                    long delay3 = 0L;
                                    if (null != tradeOrder.getDepthTs() && tradeOrder.getDepthTs() != 0L && null != tradeOrder.getDepthReceiveTs() && tradeOrder.getDepthReceiveTs() != 0L) {
                                        delay1 = delay1 + tradeOrder.getDepthReceiveTs() - tradeOrder.getDepthTs();
                                    }
                                    if (null != tradeOrder.getDepthReceiveTs() && tradeOrder.getDepthReceiveTs() != 0L && null != tradeOrder.getOpenTs() && tradeOrder.getOpenTs() != 0L) {
                                        delay2 = delay2 + tradeOrder.getOpenTs() - tradeOrder.getDepthReceiveTs();
                                    }
                                    if (null != tradeOrder.getOpenTs() && tradeOrder.getOpenTs() != 0L && null != tradeOrder.getCloseTs() && tradeOrder.getCloseTs() != 0L) {
                                        delay3 = delay3 + tradeOrder.getCloseTs() - tradeOrder.getOpenTs();
                                    }
                                    String delayTime = realOfferSymbol.getDelayTime();
                                    String consumingTime = realOfferSymbol.getConsumingTime();
                                    String pollingTime = realOfferSymbol.getPollingTime();
                                    if (null != delayTime && !delayTime.equals("")) {
                                        if(null == realOfferSymbol.getDelayCount()){
                                            realOfferSymbol.setDelayCount(0L);
                                        }
                                        realOfferSymbol.setDelayCount(realOfferSymbol.getDelayCount() + 1);
                                        if (delay1 != 0L) {
                                            delay1 = (Long.parseLong(delayTime) + delay1) / realOfferSymbol.getDelayCount();
                                        } else {
                                            delay1 = Long.parseLong(delayTime);
                                        }
                                        if (delay2 != 0L) {
                                            delay2 = (Long.parseLong(consumingTime) + delay2) / realOfferSymbol.getDelayCount();
                                        } else {
                                            delay2 = Long.parseLong(consumingTime);
                                        }
                                        if (delay3 != 0L) {
                                            delay3 = (Long.parseLong(pollingTime) + delay3) / realOfferSymbol.getDelayCount();
                                        } else {
                                            delay3 = Long.parseLong(pollingTime);
                                        }
                                    } else {
                                        realOfferSymbol.setDelayCount(1L);
                                    }
                                    realOfferSymbol.setDelayTime(String.valueOf(delay1));
                                    realOfferSymbol.setConsumingTime(String.valueOf(delay2));
                                    realOfferSymbol.setPollingTime(String.valueOf(delay3));
                                }

                                // 计算当前实盘交易总数
                                if(realOfferTradeOrderTotal.containsKey(realOffer.getId())){
                                    realOfferTradeOrderTotal.put(realOffer.getId(), realOfferTradeOrderTotal.get(realOffer.getId()) + 1);
                                } else {
                                    realOfferTradeOrderTotal.put(realOffer.getId(), 1L);
                                }
                                // 定位要添加的RealOfferExchange表数据
                                RealOfferExchange realOfferExchange = new RealOfferExchange();
                                if (exchangeResult.containsKey(realOffer.getId() + "_" + exchangeName)) {
                                    realOfferExchange = exchangeResult.get(realOffer.getId() + "_" + exchangeName);
                                    // 计算当前实盘的交易所交易总数
                                    Long oldTransactionVolumeProportionCount = realOfferExchange.getTransactionVolumeProportionCount();
                                    if(null != oldTransactionVolumeProportionCount && oldTransactionVolumeProportionCount != 0){
                                        realOfferExchange.setTransactionVolumeProportionCount(oldTransactionVolumeProportionCount+1);
                                    } else {
                                        realOfferExchange.setTransactionVolumeProportionCount(1L);
                                    }
                                } else {
                                    final LambdaQueryWrapper<RealOfferExchange> realOfferExchangeWrapper = new LambdaQueryWrapper<>();
                                    realOfferExchangeWrapper.eq(RealOfferExchange::getExchange, exchangeName);
                                    realOfferExchangeWrapper.eq(RealOfferExchange::getRealOfferId, realOffer.getId());
                                    List<RealOfferExchange> realOfferExchangeList = realOfferExchangeMapper.selectList(realOfferExchangeWrapper);
                                    if (CollectionUtils.isNotEmpty(realOfferExchangeList)) {
                                        realOfferExchange = realOfferExchangeList.get(0);
                                        Long oldTransactionVolumeProportionCount = realOfferExchange.getTransactionVolumeProportionCount();
                                        if(null != oldTransactionVolumeProportionCount && oldTransactionVolumeProportionCount != 0){
                                            realOfferExchange.setTransactionVolumeProportionCount(oldTransactionVolumeProportionCount+1);
                                        } else {
                                            realOfferExchange.setTransactionVolumeProportionCount(1L);
                                        }
                                        exchangeResult.put(realOffer.getId() + "_" + exchangeName, realOfferExchange);
                                    }
                                }
                                if (null != realOfferExchange.getId()) {
                                    // 滑点 // 成功率
                                    BigDecimal slidingPoint = new BigDecimal(0);
                                    BigDecimal closePrice = new BigDecimal(0);
                                    BigDecimal openPrice = new BigDecimal(0);
                                    BigDecimal closeAmount = new BigDecimal(0);
                                    if(null != tradeOrder.getClosePrice() && !"".equals(tradeOrder.getClosePrice())){
                                        closePrice = BigDecimal.valueOf(Double.parseDouble(tradeOrder.getClosePrice()));
                                    }
                                    if(null != tradeOrder.getOpenPrice() && !"".equals(tradeOrder.getOpenPrice())){
                                        openPrice = BigDecimal.valueOf(Double.parseDouble(tradeOrder.getOpenPrice()));
                                    }
                                    if(null != tradeOrder.getCloseAmount() && !"".equals(tradeOrder.getCloseAmount())){
                                        closeAmount = BigDecimal.valueOf(Double.parseDouble(tradeOrder.getCloseAmount()));
                                    }
                                    if (!tradeOrder.getStyle().equals("hedge") && closeAmount.compareTo(BigDecimal.ZERO) > 0) {
                                        if (tradeOrder.getSide().equals("buy")) {
                                            //(成交价-预计价)/预计价
                                            if (openPrice.compareTo(BigDecimal.ZERO) > 0 && closePrice.compareTo(BigDecimal.ZERO) > 0) {
                                                slidingPoint = slidingPoint.add(closePrice.subtract(openPrice).divide(openPrice, 4, RoundingMode.HALF_UP));
                                                slidingPoint = slidingPoint.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
                                            }
                                        } else if (tradeOrder.getSide().equals("sell")) {
                                            //(预计价-成交价)/预计价
                                            if (openPrice.compareTo(BigDecimal.ZERO) > 0 && closePrice.compareTo(BigDecimal.ZERO) > 0) {
                                                slidingPoint = slidingPoint.add(openPrice.subtract(closePrice).divide(openPrice, 4, RoundingMode.HALF_UP));
                                                slidingPoint = slidingPoint.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
                                            }
                                        }
                                        String oldSliding = realOfferExchange.getSlippage();
                                        if (null != oldSliding && !"".equals(oldSliding)) {
                                            Long oldCount = realOfferExchange.getSlippageCount();
                                            BigDecimal oldSlidingNum = BigDecimal.valueOf(Double.parseDouble(oldSliding));
                                            oldSlidingNum = oldSlidingNum.multiply(BigDecimal.valueOf(oldCount));
                                            slidingPoint = slidingPoint.add(oldSlidingNum);
                                            slidingPoint = slidingPoint.divide(BigDecimal.valueOf(oldCount + 1), 2, RoundingMode.HALF_UP);
                                            realOfferExchange.setSlippage(String.valueOf(slidingPoint));
                                            realOfferExchange.setSlippageCount(oldCount + 1);
                                        } else {
                                            realOfferExchange.setSlippage(String.valueOf(slidingPoint));
                                            realOfferExchange.setSlippageCount(1L);
                                        }
                                    }
                                    // 成功率
                                    String oldSuccessRateStr = realOfferExchange.getSuccessRate();
                                    Long successRateCount = realOfferExchange.getSuccessRateCount();
                                    BigDecimal successRate = new BigDecimal(0);
                                    if (closeAmount.compareTo(BigDecimal.ZERO) > 0) {
                                        // 本次成功
                                        successRate = successRate.add(BigDecimal.valueOf(100));
                                    }
                                    if (null != oldSuccessRateStr && !"".equals(oldSuccessRateStr)) {
                                        successRate = successRate.add(BigDecimal.valueOf(Double.parseDouble(oldSuccessRateStr)).multiply(BigDecimal.valueOf(successRateCount)));
                                        successRate = successRate.divide(BigDecimal.valueOf(successRateCount + 1), 2, RoundingMode.HALF_UP);
                                        realOfferExchange.setSuccessRate(String.valueOf(successRate));
                                        realOfferExchange.setSuccessRateCount(successRateCount + 1);
                                    } else {
                                        realOfferExchange.setSuccessRate(String.valueOf(successRate.setScale(2, RoundingMode.HALF_UP)));
                                        realOfferExchange.setSuccessRateCount(1L);
                                    }
                                }

                                // 定位要添加的RealOfferSymbolExchange表数据
                                RealOfferSymbolExchange realOfferSymbolExchange = new RealOfferSymbolExchange();
                                if (symbolExchangeResult.containsKey(realOfferSymbol.getId() + "_" + exchangeName)) {
                                    realOfferSymbolExchange = symbolExchangeResult.get(realOfferSymbol.getId() + "_" + exchangeName);
                                } else {
                                    final LambdaQueryWrapper<RealOfferSymbolExchange> realOfferSymbolExchangeWrapper = new LambdaQueryWrapper<>();
                                    realOfferSymbolExchangeWrapper.eq(RealOfferSymbolExchange::getExchange, exchangeName);
                                    realOfferSymbolExchangeWrapper.eq(RealOfferSymbolExchange::getRealOfferSymbolId, realOfferSymbol.getId());
                                    List<RealOfferSymbolExchange> realOfferSymbolExchangeList = realOfferSymbolExchangeMapper.selectList(realOfferSymbolExchangeWrapper);
                                    if (CollectionUtils.isNotEmpty(realOfferSymbolExchangeList)) {
                                        realOfferSymbolExchange = realOfferSymbolExchangeList.get(0);
                                        symbolExchangeResult.put(realOfferSymbol.getId() + "_" + exchangeName, realOfferSymbolExchange);
                                    }
                                }
                                if (null != realOfferSymbolExchange && null != realOfferSymbolExchange.getId()) {
                                    // 延时
                                    long delay1 = 0L;
                                    long delay2 = 0L;
                                    long delay3 = 0L;
                                    BigDecimal closePrice = new BigDecimal(0);
                                    BigDecimal openPrice = new BigDecimal(0);
                                    BigDecimal closeAmount = new BigDecimal(0);
                                    if (null != tradeOrder.getClosePrice() && !"".equals(tradeOrder.getClosePrice())) {
                                        closePrice = BigDecimal.valueOf(Double.parseDouble(tradeOrder.getClosePrice()));
                                    }
                                    if (null != tradeOrder.getOpenPrice() && !"".equals(tradeOrder.getOpenPrice())) {
                                        openPrice = BigDecimal.valueOf(Double.parseDouble(tradeOrder.getOpenPrice()));
                                    }
                                    if (null != tradeOrder.getCloseAmount() && !"".equals(tradeOrder.getCloseAmount())) {
                                        closeAmount = BigDecimal.valueOf(Double.parseDouble(tradeOrder.getCloseAmount()));
                                    }
                                    if (null != tradeOrder.getDepthTs() && tradeOrder.getDepthTs() != 0L && null != tradeOrder.getDepthReceiveTs() && tradeOrder.getDepthReceiveTs() != 0L) {
                                        delay1 = delay1 + tradeOrder.getDepthReceiveTs() - tradeOrder.getDepthTs();
                                    }
                                    if (null != tradeOrder.getDepthReceiveTs() && tradeOrder.getDepthReceiveTs() != 0L && null != tradeOrder.getOpenTs() && tradeOrder.getOpenTs() != 0L) {
                                        delay2 = delay2 + tradeOrder.getOpenTs() - tradeOrder.getDepthReceiveTs();
                                    }
                                    if (null != tradeOrder.getOpenTs() && tradeOrder.getOpenTs() != 0L && null != tradeOrder.getCloseTs() && tradeOrder.getCloseTs() != 0L) {
                                        delay3 = delay3 + tradeOrder.getCloseTs() - tradeOrder.getOpenTs();
                                    }
                                    String delay = realOfferSymbolExchange.getDelay();
                                    if (null != delay && !delay.equals("")) {
                                        realOfferSymbolExchange.setDelayCount(realOfferSymbolExchange.getDelayCount() + 1);
                                        String[] delayArr = delay.split("\\|");
                                        if (delay1 != 0L) {
                                            delay1 = (Long.parseLong(delayArr[0]) + delay1) / realOfferSymbolExchange.getDelayCount();
                                        } else {
                                            delay1 = Long.parseLong(delayArr[0]);
                                        }
                                        if (delay2 != 0L) {
                                            delay2 = (Long.parseLong(delayArr[1]) + delay2) / realOfferSymbolExchange.getDelayCount();
                                        } else {
                                            delay2 = Long.parseLong(delayArr[0]);
                                        }
                                        if (delay3 != 0L) {
                                            delay3 = (Long.parseLong(delayArr[2]) + delay3) / realOfferSymbolExchange.getDelayCount();
                                        } else {
                                            delay3 = Long.parseLong(delayArr[0]);
                                        }
                                    } else {
                                        realOfferSymbolExchange.setDelayCount(1L);
                                    }
                                    realOfferSymbolExchange.setDelay(delay1 + "|" + delay2 + "|" + delay3);
                                    // 总成交额
                                    BigDecimal totalTransactionAmount = new BigDecimal(0);
                                    String totalTransactionAmountStr = realOfferSymbolExchange.getTotalTransactionAmount();
                                    if (null == totalTransactionAmountStr || totalTransactionAmountStr.equals("")) {
                                        totalTransactionAmount = closePrice.multiply(closeAmount);
                                    } else {
                                        totalTransactionAmount = closePrice.multiply(closeAmount).add(BigDecimal.valueOf(Double.parseDouble(totalTransactionAmountStr)));
                                    }
                                    realOfferSymbolExchange.setTotalTransactionAmount(String.valueOf(totalTransactionAmount.setScale(2, RoundingMode.HALF_UP)));
                                    // 滑点
                                    BigDecimal slidingPoint = new BigDecimal(0);
                                    if (!tradeOrder.getStyle().equals("hedge") && closeAmount.compareTo(BigDecimal.ZERO) > 0) {
                                        if (tradeOrder.getSide().equals("buy")) {
                                            //(成交价-预计价)/预计价
                                            if (openPrice.compareTo(BigDecimal.ZERO) > 0 && closePrice.compareTo(BigDecimal.ZERO) > 0) {
                                                slidingPoint = slidingPoint.add(closePrice.subtract(openPrice).divide(openPrice, 4, RoundingMode.HALF_UP));
                                                slidingPoint = slidingPoint.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
                                            }
                                        } else if (tradeOrder.getSide().equals("sell")) {
                                            //(预计价-成交价)/预计价
                                            if (openPrice.compareTo(BigDecimal.ZERO) > 0 && closePrice.compareTo(BigDecimal.ZERO) > 0) {
                                                slidingPoint = slidingPoint.add(openPrice.subtract(closePrice).divide(openPrice, 4, RoundingMode.HALF_UP));
                                                slidingPoint = slidingPoint.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
                                            }
                                        }
                                        String oldSliding = realOfferSymbolExchange.getSlidingPoint();
                                        if (null != oldSliding && !"".equals(oldSliding)) {
                                            Long oldCount = realOfferSymbolExchange.getSlidingPointCount();
                                            BigDecimal oldSlidingNum = BigDecimal.valueOf(Double.parseDouble(oldSliding));
                                            oldSlidingNum = oldSlidingNum.multiply(BigDecimal.valueOf(oldCount));
                                            slidingPoint = slidingPoint.add(oldSlidingNum);
                                            slidingPoint = slidingPoint.divide(BigDecimal.valueOf(oldCount + 1), 2, RoundingMode.HALF_UP);
                                            realOfferSymbolExchange.setSlidingPoint(String.valueOf(slidingPoint));
                                            realOfferSymbolExchange.setSlidingPointCount(oldCount + 1);
                                        } else {
                                            realOfferSymbolExchange.setSlidingPoint(String.valueOf(slidingPoint));
                                            realOfferSymbolExchange.setSlidingPointCount(1L);
                                        }
                                    }
                                    // 成功率
                                    String oldSuccessRateStr = realOfferSymbolExchange.getSuccessRate();
                                    Long successRateCount = realOfferSymbolExchange.getSuccessRateCount();
                                    BigDecimal successRate = new BigDecimal(0);
                                    if (closeAmount.compareTo(BigDecimal.ZERO) > 0) {
                                        // 本次成功
                                        successRate = successRate.add(BigDecimal.valueOf(100));
                                    }
                                    if (null != oldSuccessRateStr && !"".equals(oldSuccessRateStr)) {
                                        successRate = successRate.add(BigDecimal.valueOf(Double.parseDouble(oldSuccessRateStr)).multiply(BigDecimal.valueOf(successRateCount)));
                                        successRate = successRate.divide(BigDecimal.valueOf(successRateCount + 1), 2, RoundingMode.HALF_UP);
                                        realOfferSymbolExchange.setSuccessRate(String.valueOf(successRate));
                                        realOfferSymbolExchange.setSuccessRateCount(successRateCount + 1);
                                    } else {
                                        realOfferSymbolExchange.setSuccessRate(String.valueOf(successRate.setScale(2, RoundingMode.HALF_UP)));
                                        realOfferSymbolExchange.setSuccessRateCount(1L);
                                    }

                                }
                            }
                        }
                    }
                }
            }
            // 计算完成更新RealOfferSymbol表数据
            for (Map.Entry<String, RealOfferSymbol> entry : symbolResult.entrySet()) {
                realOfferSymbolMapper.updateById(entry.getValue());
            }
            // 计算完成更新RealOfferSymbolExchange表数据
            for (Map.Entry<String, RealOfferSymbolExchange> entry : symbolExchangeResult.entrySet()) {
                realOfferSymbolExchangeMapper.updateById(entry.getValue());
            }
            // 计算完成更新RealOfferExchange表数据
            for (Map.Entry<String, RealOfferExchange> entry : exchangeResult.entrySet()) {
                RealOfferExchange realOfferExchange = entry.getValue();
                if(null != realOfferExchange.getTransactionVolumeProportionCount()){
                    BigDecimal realOfferExchangeTraningTotal = BigDecimal.valueOf(realOfferExchange.getTransactionVolumeProportionCount());
                    BigDecimal realOfferExchangeTraningPro = new BigDecimal(0);
                    if(realOfferTradeOrderTotal.containsKey(realOfferExchange.getRealOfferId())){
                        Long realOfferTraningTotal = realOfferTradeOrderTotal.get(realOfferExchange.getRealOfferId());
                        realOfferExchangeTraningPro = realOfferExchangeTraningTotal.divide(BigDecimal.valueOf(realOfferTraningTotal), 4, RoundingMode.HALF_UP);
                        realOfferExchange.setTransactionVolumeProportion(String.valueOf(realOfferExchangeTraningPro.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)));
                    }
                }
                realOfferExchangeMapper.updateById(entry.getValue());
            }
            // 计算完成更新计算锚点
            Trade lastTrade = tradeList.get(tradeList.size() - 1);
            timedTaskAnchorPoint.setRealOfferSymbolExchangeTradeId(lastTrade.getId());
            timedTaskAnchorPointMapper.updateById(timedTaskAnchorPoint);
        }
        System.out.println("==========================币-交易所数据计算完毕================================");
        System.out.println(new Date());
    }

    @Scheduled(fixedRate = 2 * 60 * 1000, initialDelay = 60 * 1000)
    public void setRealOfferSymbolInfo() {

    }

    @Scheduled(fixedRate = 5 * 60 * 1000, initialDelay = 10 * 1000)
    public void setRealOfferExchangeInfo() {
        System.out.println("==========================开始定时计算实盘-交易所数据================================");
        System.out.println(new Date());
        Map<String, RealOfferExchange> realOfferExchangeInfo = new HashMap<>();
        List<RealOfferExchange> realOfferExchangeList = new ArrayList<>();
        Map<String, Long> realOfferInfo = new HashMap<>();
        Map<Long, List<Long>> realOfferSymbolIdListInfo = new HashMap<>();
        final LambdaQueryWrapper<RealOffer> realOfferWrapper = new LambdaQueryWrapper<>();
        realOfferWrapper.eq(RealOffer::getDelFlag, IS_EXIST);
        List<RealOffer> realOfferList = realOfferMapper.selectList(realOfferWrapper);
        List<Long> realOfferIdList = new ArrayList<>();
        List<String> realOfferAppIdList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(realOfferList)){
            for(RealOffer realOffer : realOfferList){
                realOfferIdList.add(realOffer.getId());
                realOfferAppIdList.add(realOffer.getAppId());
                realOfferInfo.put(realOffer.getAppId(), realOffer.getId());
                final LambdaQueryWrapper<RealOfferSymbol> realOfferSymbolWrapper = new LambdaQueryWrapper<>();
                realOfferSymbolWrapper.eq(RealOfferSymbol::getRealOfferId, realOffer.getId());
                List<Long> realOfferSymbolIdList = realOfferSymbolMapper.selectList(realOfferSymbolWrapper).stream().map(RealOfferSymbol::getId).toList();
                realOfferSymbolIdListInfo.put(realOffer.getId(), realOfferSymbolIdList);
            }
        }

        // 定位RealOfferExchange表数据
        if(CollectionUtils.isNotEmpty(realOfferIdList)){
            final LambdaQueryWrapper<RealOfferExchange> realOfferExchangeWrapper = new LambdaQueryWrapper<>();
            realOfferExchangeWrapper.in(RealOfferExchange::getRealOfferId,realOfferIdList);
            realOfferExchangeList = realOfferExchangeMapper.selectList(realOfferExchangeWrapper);
            if(CollectionUtils.isNotEmpty(realOfferExchangeList)){
                for(RealOfferExchange realOfferExchange : realOfferExchangeList){
                    realOfferExchangeInfo.put(realOfferExchange.getRealOfferId()+"-"+realOfferExchange.getExchange(), realOfferExchange);
                    // 交易对数
                    final LambdaQueryWrapper<RealOfferSymbolExchange> realOfferSymbolExchangeWrapper = new LambdaQueryWrapper<>();
                    realOfferSymbolExchangeWrapper.eq(RealOfferSymbolExchange::getExchange, realOfferExchange.getExchange());
                    realOfferSymbolExchangeWrapper.in(RealOfferSymbolExchange::getRealOfferSymbolId, realOfferSymbolIdListInfo.get(realOfferExchange.getRealOfferId()));
                    Long aLong = realOfferSymbolExchangeMapper.selectCount(realOfferSymbolExchangeWrapper);
                    realOfferExchange.setTransactionPairs(String.valueOf(aLong));
                }
            }
        }

        // 日交易量(U)
        // 日交易量(挂|吃)
        if(CollectionUtils.isNotEmpty(realOfferAppIdList)){
            final LambdaQueryWrapper<Trade> tradeWrapper = new LambdaQueryWrapper<>();
            Date date = new Date();
            long time = date.getTime();
            tradeWrapper.ge(Trade::getCreateTime, new Date(time - 1000*60*60*24));
            tradeWrapper.in(Trade::getAppId, realOfferAppIdList);
            List<Trade> tradeList = tradeMapper.selectList(tradeWrapper);
            if(CollectionUtils.isNotEmpty(tradeList)){
                Map<Long, String> exchangeIdUsdtInfo = new HashMap<>();
                for(Trade trade : tradeList){
                    Long realOfferId = realOfferInfo.get(trade.getAppId());
                    if(null != realOfferId){
                        final LambdaQueryWrapper<TradeOrder> tradeOrderWrapper = new LambdaQueryWrapper<>();
                        tradeOrderWrapper.eq(TradeOrder::getTradeId, trade.getTradeId());
                        List<TradeOrder> tradeOrderList = tradeOrderMapper.selectList(tradeOrderWrapper);
                        String exchangeName = "";
                        if (trade.getMode().equals("hedge")) {
                            exchangeName = null != tradeOrderList.get(0).getExchange() ? tradeOrderList.get(0).getExchange() : tradeOrderList.get(1).getExchange();
                        }
                        if(CollectionUtils.isNotEmpty(tradeOrderList)){
                            for(TradeOrder tradeOrder : tradeOrderList){
                                if ("".equals(exchangeName)) {
                                    exchangeName = tradeOrder.getExchange();
                                }
                                RealOfferExchange realOfferExchange = realOfferExchangeInfo.get(realOfferId + "-" + exchangeName);
                                if(null != realOfferExchange && null!=realOfferExchange.getId()){
                                    BigDecimal currentTradingUsdt = new BigDecimal(0);
                                    BigDecimal closePrice = BigDecimal.valueOf(Double.parseDouble(tradeOrder.getClosePrice()));
                                    BigDecimal closeAmount = BigDecimal.valueOf(Double.parseDouble(tradeOrder.getCloseAmount()));
                                    currentTradingUsdt = currentTradingUsdt.add(closePrice.multiply(closeAmount));
                                    String usdt = exchangeIdUsdtInfo.get(realOfferExchange.getId());
                                    if(null != usdt && !"".equals(usdt)){
                                        currentTradingUsdt = currentTradingUsdt.add(BigDecimal.valueOf(Double.parseDouble(usdt)));
                                    }
                                    exchangeIdUsdtInfo.put(realOfferExchange.getId(), String.valueOf(currentTradingUsdt.setScale(1,RoundingMode.HALF_UP)));
                                }
                            }
                        }
                    }
                }
                if(CollectionUtils.isNotEmpty(realOfferExchangeList)){
                    for(RealOfferExchange realOfferExchange : realOfferExchangeList) {
                        realOfferExchange.setDailyTradingVolumeUsdt(exchangeIdUsdtInfo.get(realOfferExchange.getId()));
                        realOfferExchange.setDailyTradingVolume("0|"+realOfferExchange.getDailyTradingVolumeUsdt());
                    }
                }
            }
        }

        // 计算完成 更新RealOfferExchange表数据
        for(RealOfferExchange realOfferExchange : realOfferExchangeList){
            realOfferExchangeMapper.updateById(realOfferExchange);
        }
        System.out.println("==========================实盘-交易所数据计算完毕================================");
        System.out.println(new Date());
    }

}
