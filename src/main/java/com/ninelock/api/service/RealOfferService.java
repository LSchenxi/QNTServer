package com.ninelock.api.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ninelock.api.core.auth.Session;
import com.ninelock.api.entity.*;
import com.ninelock.api.mapper.*;
import com.ninelock.api.request.DeleteReq;
import com.ninelock.api.request.RealOfferCreateReq;
import com.ninelock.api.request.RealOfferReq;
import com.ninelock.api.request.RealOfferBaseUpdateReq;
import com.ninelock.api.response.*;
import com.ninelock.api.utils.AESUtil;
import com.ninelock.api.utils.SendHttpReqUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.ninelock.api.core.auth.Session.SESSION_ID;
import static com.ninelock.api.core.constant.EntityConstant.IS_EXIST;

@Slf4j
@Service
public class RealOfferService extends ServiceImpl<RealOfferMapper, RealOffer> {

    @Resource
    private ServerMapper serverMapper;
    @Resource
    private StrategyMapper strategyMapper;
    @Resource
    private ExchangeDetailMapper exchangeDetailMapper;
    @Resource
    private RealOfferExchangeMapper realOfferExchangeMapper;
    @Resource
    private ExchangeMapper exchangeMapper;
    @Resource
    private TradeMapper tradeMapper;
    @Resource
    private RealOfferSymbolMapper realOfferSymbolMapper;
    @Resource
    private SymbolMapper symbolMapper;
    @Resource
    private RealOfferSymbolExchangeMapper realOfferSymbolExchangeMapper;
    @Value("${qnt_robot.ip}")
    private String qntRobotIp;
    @Value("${qnt_robot.port}")
    private String qntRobotPort;

    public Result<?> getPage(RealOfferReq req) {
        final Session session = (Session) StpUtil.getSession().get(SESSION_ID);
        Long userId = session.getUserId();
        final int page = req.getPage();
        final int size = req.getSize();

        // 查询分页列表
        final LambdaQueryWrapper<RealOffer> wrapper = new LambdaQueryWrapper<>();
        if (null != req.getName() && !"".equals(req.getName())) {
            wrapper.like(RealOffer::getName, req.getName());
        }
        wrapper.eq(RealOffer::getDelFlag, IS_EXIST);
        wrapper.eq(RealOffer::getCreateId, userId);
        final Page<RealOffer> realOfferPage = this.page(new Page<>(page, size), wrapper);
        // 转为响应对象
        final List<RealOfferResp> strategyRespList = realOfferPage.getRecords().stream().map(record -> {
            final RealOfferResp realOfferResp = new RealOfferResp();
            BeanUtils.copyProperties(record, realOfferResp);
            // 挂载服务器ip
            if (null != record.getServerId()) {
                Server server = serverMapper.selectById(record.getServerId());
                if (null != server) {
                    realOfferResp.setServerIp(server.getServerIp());
                } else {
                    realOfferResp.setServerIp("");
                }
            } else {
                realOfferResp.setServerIp("");
            }
            // 执行策略名称
            if (null != record.getStrategyId()) {
                Strategy strategy = strategyMapper.selectById(record.getStrategyId());
                if (null != strategy) {
                    realOfferResp.setStrategyName(strategy.getName());
                } else {
                    realOfferResp.setStrategyName("");
                }
            } else {
                realOfferResp.setStrategyName("");
            }
            if(null != record.getAppId() && !record.getAppId().equals("")){
                final LambdaQueryWrapper<Trade> tradeWrapper = new LambdaQueryWrapper<>();
                tradeWrapper.eq(Trade::getAppId, record.getAppId());
                tradeWrapper.orderByDesc(Trade::getId);
                Page<Trade> tradePage = new Page<>(1,1);
                tradePage.setRecords(tradeMapper.selectPage(tradePage, tradeWrapper).getRecords());
                Trade trade = tradePage.getRecords().isEmpty() ? null : tradePage.getRecords().get(0);
                if(trade != null){
                    realOfferResp.setProfit(Double.valueOf(trade.getAppProfit()));
                }
            }

            return realOfferResp;
        }).toList();
        final RealOfferPageResp realOfferPageResp = new RealOfferPageResp();
        realOfferPageResp.setTotal(realOfferPage.getTotal());
        realOfferPageResp.setRecords(strategyRespList);
        return Result.ok(realOfferPageResp);
    }

    public Result<?> getRealOffer(Long id) {
        return Result.ok(this.baseMapper.selectById(id));
    }

    public Result<?> createRealOffer(RealOfferCreateReq req) {
        List<Map<String, Long>> exchangeMapList = req.getExchangeMapList();
        List<Long> existExchangeList = new ArrayList<>();
        Boolean existFlag = false;
        for (Map<String, Long> exchangeMap : exchangeMapList) {
            Long exchangeDetailId = exchangeMap.get("exchange");
            ExchangeDetail exchangeDetail = exchangeDetailMapper.selectById(exchangeDetailId);
            if(!existExchangeList.contains(exchangeDetail.getExchangeId())){
                existExchangeList.add(exchangeDetail.getExchangeId());
            } else {
                existFlag = true;
                break;
            }
        }
        if(existFlag){
            return Result.error("交易所配置重复", null);
        }
        final Session session = (Session) StpUtil.getSession().get(SESSION_ID);
        Long userId = session.getUserId();
        final LambdaQueryWrapper<RealOffer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RealOffer::getName, req.getName());
        wrapper.eq(RealOffer::getDelFlag, IS_EXIST);
        wrapper.eq(RealOffer::getCreateId, userId);
        long count = this.count(wrapper);
        if (count > 0) {
            return Result.error("实盘名称重复", null);
        }
        final LambdaQueryWrapper<RealOffer> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(RealOffer::getAppId, req.getAppId());
        wrapper2.eq(RealOffer::getDelFlag, IS_EXIST);
        wrapper2.eq(RealOffer::getCreateId, userId);
        long count2 = this.count(wrapper2);
        if (count2 > 0) {
            return Result.error("实盘程序索引重复", null);
        }
        RealOffer realOffer = new RealOffer();
        realOffer.setName(req.getName());
        realOffer.setRealOfferPort(req.getRealOfferPort());
        realOffer.setServerId(req.getServerId());
        realOffer.setStrategyId(req.getStrategyId());
        realOffer.setStatus("已停止");
        realOffer.setProfit(0.0);
        realOffer.setOpenFlag(1);
        realOffer.setKLinePeriod(req.getLinePeriod());
        realOffer.setKLineUnit(req.getLineUnit());
        realOffer.setAppId(req.getAppId());
        // 增加默认参数(自动交易参数)
        realOffer.setAutoTrading(1);
        realOffer.setBasicWarehouseValue("200");
        realOffer.setMaxHoldingNumber("58");
        realOffer.setChangeSymbolProtectPeriod("8");
        realOffer.setChangeSymbolWatchPeriod("8");
        realOffer.setMaxChangeSymbolWatchNumber("19");
        realOffer.setChangeSymbolSilentPeriod("24");
        realOffer.setChangeSymbolMinMonthly("0.15");
        realOffer.setSymbolFilter(0);
        realOffer.setBiasAveragePeriod("8H");
        realOffer.setMinBias("-0.3");
        realOffer.setMaxBias("0.3");
        realOffer.setAutoAddWarehouse(0);
        realOffer.setAddRatio("0.5");
        realOffer.setMaxAddMultiple("2");
        realOffer.setAutoReduceWarehouse(0);
        realOffer.setReduceRatio("1");
        realOffer.setClearWarehouseProtect(0);
        realOffer.setMaxClearProtectNumber("50");
        realOffer.setMinHoldingPhaseRate("-0.03");
        realOffer.setMaxHoldingContainClearProtect(1);
        // 增加默认参数(交易参数)
        realOffer.setInitialAsset("10000");
        realOffer.setExtractAsset("1000");
        realOffer.setMinTransactionOne("10");
        realOffer.setMaxTransactionOne("1000");
        realOffer.setArbitragePremium("0.001");
        realOffer.setInitialEquilibriumPremium("0");
        realOffer.setMaxEquilibriumLossPremium("0.01");
        realOffer.setOrderFlag(0);
        realOffer.setMaxPendingOrder("50");
        realOffer.setOrderDistance("2.5");
        realOffer.setMaxDiffSameSymbolFilter("0.1");
        realOffer.setPlatformDeductionCoin("20");
        realOffer.setCreateId(userId);
        realOffer.setDelFlag(IS_EXIST);
        if (this.save(realOffer)) {
            Server server = serverMapper.selectById(realOffer.getServerId());
            server.setRealOfferNumber(server.getRealOfferNumber() + 1);
            serverMapper.updateById(server);
            for (Map<String, Long> exchangeMap : exchangeMapList) {
                Long exchange = exchangeMap.get("exchange");
                RealOfferExchange realOfferExchange = new RealOfferExchange();
                realOfferExchange.setExchangeId(exchange);
                String exchangeName = exchangeMapper.selectById(exchangeDetailMapper.selectById(exchange).getExchangeId()).getExchange();
                realOfferExchange.setExchange(exchangeName);
                realOfferExchange.setRealOfferId(realOffer.getId());
                realOfferExchange.setStatus(1);
                realOfferExchangeMapper.insert(realOfferExchange);
            }
            return Result.ok();
        } else {
            return Result.error("保存失败", null);
        }
    }

    public Result<?> updateRealOffer(RealOfferBaseUpdateReq req) {
//        final LambdaQueryWrapper<RealOffer> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(RealOffer::getName, req.getName());
//        wrapper.eq(RealOffer::getDelFlag, IS_EXIST);
//        wrapper.ne(RealOffer::getId, req.getId());
//        long count = this.count(wrapper);
//        if (count > 0) {
//            return Result.error("实盘名称重复", null);
//        }
//        RealOffer realOffer = new RealOffer();
//        BeanUtils.copyProperties(req, realOffer);
//        if (this.updateById(realOffer)) {
//            return Result.ok();
//        } else {
//            return Result.error("更新失败", null);
//        }
        return Result.ok();
    }

    public Result<?> stopRealOffer(DeleteReq req) {
        RealOffer realOffer = this.baseMapper.selectById(req.getId());
        Server server = serverMapper.selectById(realOffer.getServerId());

        // 临时
        realOffer.setStatus("已停止");
        this.baseMapper.updateById(realOffer);
        return Result.ok();
        /*// http指令
        String reqUrl = "http://" + server.getServerIp() + ":" + realOffer.getRealOfferPort() + "/api/v1/stop";
        HttpResponse response = HttpRequest.post(reqUrl).header(Header.CONTENT_TYPE, "application/json").timeout(60 * 1000).execute();
        System.out.println(response.body());
        if(response.isOk()){
            String result = response.body();
            QntRobotResponse qntRobotResponse = JSONUtil.toBean(result, QntRobotResponse.class);
            System.out.println(qntRobotResponse.getSuccess() + ":" + result);
            if(qntRobotResponse.getSuccess()){
                // 数据库操作
                realOffer.setStatus("已停止");
                if (this.baseMapper.updateById(realOffer) == 1) {
                    return Result.ok();
                } else {
                    return Result.error("停止失败", null);
                }
            } else {
                return Result.error("停止失败", null);
            }
        } else {
            return Result.error("停止失败", null);
        }*/
    }

    public Result<?> startRealOffer(DeleteReq req) {
        Date now = new Date();
        boolean httpFlag = false;
        RealOffer realOffer = this.baseMapper.selectById(req.getId());
        // http指令 发送交易所列表
        Server server = serverMapper.selectById(realOffer.getServerId());
        Map<String, Object> reqForm = new HashMap<>();
        List<Map<String, String>> exchangeList = new ArrayList<>();
        exchangeList = getExchangeList(realOffer);
        reqForm.put("exchanges", exchangeList);
        String jsonStr = JSONUtil.toJsonStr(reqForm);
        String key = AESUtil.getKey(realOffer.getAppId());
        String encrypt = AESUtil.encryptCBC(jsonStr, key);
        String reqUrl = "http://" + server.getServerIp() + ":" + realOffer.getRealOfferPort() + "/api/v1/start";
//        String reqUrl = "http://" + qntRobotIp + ":" + realOffer.getRealOfferPort() + "/api/v1/start";
        System.out.println(jsonStr);
        System.out.println(encrypt);
        System.out.println(jsonStr.length());
        try {
            HttpResponse response = HttpRequest.post(reqUrl).header(Header.CONTENT_TYPE, "application/json").body(encrypt).header("Data-Length", String.valueOf(jsonStr.length())).timeout(60 * 1000).execute();
            String result = response.body();
            System.out.println(result);
            QntRobotResponse qntRobotResponse = JSONUtil.toBean(result, QntRobotResponse.class);
            if(response.isOk() && qntRobotResponse.getSuccess()){
                realOffer.setStatus("运行中");
                this.baseMapper.updateById(realOffer);
            } else {
                return Result.error("启动失败", null);
            }
            System.out.println(jsonStr);
            System.out.println(response.body());
        } catch (Exception e){
            httpFlag = true;

        }
        if(httpFlag){
            return Result.error("启动失败", null);
        }
        // http指令 循环发送添加交易对 （页面启动币）
        final LambdaQueryWrapper<RealOfferSymbol> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RealOfferSymbol::getRealOfferId, realOffer.getId());
        wrapper.eq(RealOfferSymbol::getSymbolStatus, 1);
        List<RealOfferSymbol> realOfferSymbols = realOfferSymbolMapper.selectList(wrapper);
        for(RealOfferSymbol realOfferSymbol : realOfferSymbols){
            realOfferSymbol.setOperationTime(now);
            Symbol symbol = symbolMapper.selectById(realOfferSymbol.getSymbolId());
            Map<String, Object> reqForm2 = new HashMap<>();
            List<Map<String, Object>> exchangeList2 = new ArrayList<>();
            exchangeList2 = getExchangeNameList(realOfferSymbol, symbol, realOffer.getId());
            reqForm2.put("symbol", symbol.getName());
            reqForm2.put("exchanges", exchangeList2);
//            reqForm2.put("pricePrecision", symbol.getPricePrecision());
//            reqForm2.put("amountPrecision", symbol.getAmountPrecision());
            String jsonStr2 = JSONUtil.toJsonStr(reqForm2);
            String key2 = AESUtil.getKey(realOffer.getAppId());
            String encrypt2 = AESUtil.encryptCBC(jsonStr2, key2);
            String reqUrl2 = "http://" + server.getServerIp() + ":" + realOffer.getRealOfferPort() + "/api/v1/newSymbol";
//            HttpRequest.post(reqUrl2).body(encrypt2).header(Header.CONTENT_TYPE, "application/json").header("Data-Length", String.valueOf(jsonStr.length())).timeout(60 * 1000).execute();
            // 改为异步方法
            SendHttpReqUtil sendHttpReqUtil = new SendHttpReqUtil(reqUrl2, encrypt2, String.valueOf(jsonStr2.length()), realOfferSymbolMapper, realOfferSymbol);
            Thread send = new Thread(sendHttpReqUtil);
            send.start();
        }
        return Result.ok();
    }

    public List<Map<String, String>> getExchangeList(RealOffer realOffer) {
        List<Map<String, String>> exchangeList = new ArrayList<>();
        final LambdaQueryWrapper<RealOfferExchange> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RealOfferExchange::getRealOfferId, realOffer.getId());
        List<Long> longs = realOfferExchangeMapper.selectList(wrapper).stream().map(RealOfferExchange::getExchangeId).toList();
        final LambdaQueryWrapper<ExchangeDetail> exchangeDetailWrapper = new LambdaQueryWrapper<>();
        exchangeDetailWrapper.in(ExchangeDetail::getId, longs);
        List<ExchangeDetail> exchangeDetails = exchangeDetailMapper.selectList(exchangeDetailWrapper);
        for(ExchangeDetail exchangeDetail : exchangeDetails){
            Map<String, String> exchange = new HashMap<>();
            Exchange exchange1 = exchangeMapper.selectById(exchangeDetail.getExchangeId());
            exchange.put("name", exchange1.getExchange());
            exchange.put("apiKey", exchangeDetail.getAccessKey());
            exchange.put("apiSecret", exchangeDetail.getSecretKey());
            if("kucoin".equals(exchange1.getExchange())){
                exchange.put("apiPassphrase", exchangeDetail.getPassword());
            } else {
                exchange.put("apiPassphrase", exchangeDetail.getPassphrase());
            }
            exchangeList.add(exchange);
        }
        return exchangeList;
    }

    public List<Map<String, Object>> getExchangeNameList(RealOfferSymbol realOfferSymbol, Symbol symbol, Long realOfferId) {
        List<Map<String, Object>> exchangeList = new ArrayList<>();
        final LambdaQueryWrapper<RealOfferSymbolExchange> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RealOfferSymbolExchange::getRealOfferSymbolId, realOfferSymbol.getId());
        exchangeList = realOfferSymbolExchangeMapper.selectList(wrapper).stream().map(record -> {
            Map<String, Object> exchageInfo = new HashMap<>();
            exchageInfo.put("name", record.getExchange());
            if(record.getExchange().equals("mexc")){
                exchageInfo.put("pricePrecision", symbol.getExMexcPrice());
                exchageInfo.put("amountPrecision", symbol.getExMexcAmount());
            }
            if(record.getExchange().equals("gate")){
                exchageInfo.put("pricePrecision", symbol.getExGatePrice());
                exchageInfo.put("amountPrecision", symbol.getExGateAmount());
            }
            if(record.getExchange().equals("kucoin")){
                exchageInfo.put("pricePrecision", symbol.getExKucoinPrice());
                exchageInfo.put("amountPrecision", symbol.getExKucoinAmount());
            }
            if(record.getExchange().equals("htx")){
                exchageInfo.put("pricePrecision", symbol.getExHtxPrice());
                exchageInfo.put("amountPrecision", symbol.getExHtxAmount());
            }
            if(record.getExchange().equals("bitget")){
                exchageInfo.put("pricePrecision", symbol.getExBitgetPrice());
                exchageInfo.put("amountPrecision", symbol.getExBitgetAmount());
            }
            if(record.getExchange().equals("okx")){
                exchageInfo.put("pricePrecision", symbol.getExOkxPrice());
                exchageInfo.put("amountPrecision", symbol.getExOkxAmount());
            }
            if(record.getExchange().equals("bybit")){
                exchageInfo.put("pricePrecision", symbol.getExBybitPrice());
                exchageInfo.put("amountPrecision", symbol.getExBybitAmount());
            }
            if(record.getExchange().equals("coinex")){
                exchageInfo.put("pricePrecision", symbol.getExCoinexPrice());
                exchageInfo.put("amountPrecision", symbol.getExCoinexAmount());
            }
            if(record.getExchange().equals("bitmart")){
                exchageInfo.put("pricePrecision", symbol.getExBitmartPrice());
                exchageInfo.put("amountPrecision", symbol.getExBitmartAmount());
            }
            return exchageInfo;
        }).toList();
        return exchangeList;
    }

    public List<String> getExchangeNameList(Symbol symbol) {
        List<String> exchangeList = new ArrayList<>();
        if (symbol.getExMexc() == 1) {
            exchangeList.add("mexc");
        }
        if (symbol.getExGate() == 1) {
            exchangeList.add("gate");
        }
        if (symbol.getExKucoin() == 1) {
            exchangeList.add("kucoin");
        }
        if (symbol.getExHtx() == 1) {
            exchangeList.add("htx");
        }
        if (symbol.getExBitget() == 1) {
            exchangeList.add("bitget");
        }
        if (symbol.getExOkx() == 1) {
            exchangeList.add("okx");
        }
        if (symbol.getExBybit() == 1) {
            exchangeList.add("bybit");
        }
        if (symbol.getExCoinex() == 1) {
            exchangeList.add("coinex");
        }
        if (symbol.getExBitmart() == 1) {
            exchangeList.add("bitmart");
        }
        return exchangeList;
    }

    public Result<?> deleteRealOffer(DeleteReq req) {
        RealOffer realOffer = this.baseMapper.selectById(req.getId());
        Server server = serverMapper.selectById(realOffer.getServerId());
        server.setRealOfferNumber(server.getRealOfferNumber() - 1);
        serverMapper.updateById(server);
        if (this.removeById(req.getId())) {
            final LambdaQueryWrapper<RealOfferExchange> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RealOfferExchange::getRealOfferId, req.getId());
            realOfferExchangeMapper.delete(wrapper);
            return Result.ok();
        } else {
            return Result.error("删除失败", null);
        }
    }

    public Result<?> getServerList() {
        final Session session = (Session) StpUtil.getSession().get(SESSION_ID);
        Long userId = session.getUserId();
        final LambdaQueryWrapper<Server> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Server::getDelFlag, IS_EXIST);
        wrapper.eq(Server::getCreateId, userId);
        List<Server> servers = serverMapper.selectList(wrapper);
        if (CollectionUtils.isNotEmpty(servers)) {
            List<ServerSelectResp> serverSelectResps = servers.stream().map(record -> {
                ServerSelectResp serverSelectResp = new ServerSelectResp();
                serverSelectResp.setLabel(record.getServerIp());
                serverSelectResp.setValue(record.getId());
                return serverSelectResp;
            }).toList();
            return Result.ok(serverSelectResps);
        }
        return Result.error("暂无数据", null);
    }

    public Result<?> getStrategyList() {
        final Session session = (Session) StpUtil.getSession().get(SESSION_ID);
        Long userId = session.getUserId();
        final LambdaQueryWrapper<Strategy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Strategy::getDelFlag, IS_EXIST);
        wrapper.eq(Strategy::getCreateId, userId);
        List<Strategy> strategies = strategyMapper.selectList(wrapper);
        if (CollectionUtils.isNotEmpty(strategies)) {
            List<StrategySelectResp> strategySelectResps = strategies.stream().map(record -> {
                StrategySelectResp strategySelectResp = new StrategySelectResp();
                strategySelectResp.setLabel(record.getName());
                strategySelectResp.setValue(record.getId());
                return strategySelectResp;
            }).toList();
            return Result.ok(strategySelectResps);
        }
        return Result.error("暂无数据", null);
    }

    public Result<?> getExchangeList() {
        final Session session = (Session) StpUtil.getSession().get(SESSION_ID);
        Long userId = session.getUserId();
        final LambdaQueryWrapper<ExchangeDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExchangeDetail::getDelFlag, IS_EXIST);
        wrapper.eq(ExchangeDetail::getCreateId, userId);
        List<ExchangeDetail> exchanges = exchangeDetailMapper.selectList(wrapper);
        if (CollectionUtils.isNotEmpty(exchanges)) {
            List<ExchangeSelectResp> exchangeSelectResps = exchanges.stream().map(record -> {
                ExchangeSelectResp exchangeSelectResp = new ExchangeSelectResp();
                exchangeSelectResp.setLabel(record.getExchangeLabel());
                exchangeSelectResp.setValue(record.getId());
                final LambdaQueryWrapper<RealOfferExchange> realOfferExchangeWrapper = new LambdaQueryWrapper<>();
                realOfferExchangeWrapper.eq(RealOfferExchange::getExchangeId, record.getId());
                realOfferExchangeWrapper.eq(RealOfferExchange::getDelFlag, IS_EXIST);
                Long aLong = realOfferExchangeMapper.selectCount(realOfferExchangeWrapper);
                if(aLong > 0){
                    exchangeSelectResp.setDisabled(true);
                }
                return exchangeSelectResp;
            }).toList();
            return Result.ok(exchangeSelectResps);
        }
        return Result.error("暂无数据", null);
    }

    public Result<?> getTradingCurrencyList() {
        //已废弃 原获取币list接口
//        final LambdaQueryWrapper<TradingCurrency> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(TradingCurrency::getDelFlag, IS_EXIST);
//        List<TradingCurrency> tradingCurrencies = tradingCurrencyMapper.selectList(wrapper);
//        if (CollectionUtils.isNotEmpty(tradingCurrencies)) {
//            List<SymbolSelectResp> symbolSelectResps = tradingCurrencies.stream().map(record -> {
//                SymbolSelectResp symbolSelectResp = new SymbolSelectResp();
//                symbolSelectResp.setLabel(record.getName());
//                symbolSelectResp.setValue(record.getId());
//                return symbolSelectResp;
//            }).toList();
//            return Result.ok(symbolSelectResps);
//        }
        return Result.error("暂无数据", null);
    }
}
