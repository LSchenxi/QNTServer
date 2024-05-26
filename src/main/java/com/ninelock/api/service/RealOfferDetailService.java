package com.ninelock.api.service;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ninelock.api.entity.*;
import com.ninelock.api.mapper.*;
import com.ninelock.api.request.*;
import com.ninelock.api.response.*;
import com.ninelock.api.utils.AESUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.ninelock.api.core.constant.EntityConstant.IS_EXIST;

@Slf4j
@Service
public class RealOfferDetailService extends ServiceImpl<RealOfferMapper, RealOffer> {

    @Resource
    private StrategyMapper strategyMapper;
    @Resource
    private RealOfferExchangeMapper realOfferExchangeMapper;
    @Resource
    private RealOfferSymbolMapper realOfferSymbolMapper;
    @Resource
    private ExchangeDetailMapper exchangeDetailMapper;
    @Resource
    private ExchangeMapper exchangeMapper;
    @Resource
    private SymbolMapper symbolMapper;
    @Resource
    private TradeMapper tradeMapper;
    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private RealOfferSymbolExchangeMapper realOfferSymbolExchangeMapper;
    @Resource
    private RealOfferRunLogMapper realOfferRunLogMapper;
    @Resource
    private ServerMapper serverMapper;
    @Resource
    private AssetMapper assetMapper;
    @Value("${qnt_robot.ip}")
    private String qntRobotIp;
    @Value("${qnt_robot.port}")
    private String qntRobotPort;

    public Result<?> getRealOffer(Long realOfferId) {
        RealOffer realOffer = this.baseMapper.selectById(realOfferId);
        final RealOfferDetailResp realOfferDetailResp = new RealOfferDetailResp();
        BeanUtils.copyProperties(realOffer, realOfferDetailResp);
        // 执行策略名称 策略修改时间
        if (null != realOffer.getStrategyId()) {
            Strategy strategy = strategyMapper.selectById(realOffer.getStrategyId());
            if (null != strategy) {
                realOfferDetailResp.setStrategyName(strategy.getName());
                realOfferDetailResp.setStrategyUpdateTime(strategy.getUpdateTime());
            } else {
                realOfferDetailResp.setStrategyName("");
            }
        } else {
            realOfferDetailResp.setStrategyName("");
        }
        // 交易所名称列表
        final LambdaQueryWrapper<RealOfferExchange> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RealOfferExchange::getDelFlag, IS_EXIST);
        wrapper.eq(RealOfferExchange::getRealOfferId, realOfferId);
        List<Long> exchangeIdList = realOfferExchangeMapper.selectList(wrapper).stream().map(RealOfferExchange::getExchangeId).toList();
        final LambdaQueryWrapper<ExchangeDetail> exchangeWrapper = new LambdaQueryWrapper<>();
        exchangeWrapper.in(ExchangeDetail::getId, exchangeIdList);
        exchangeWrapper.in(ExchangeDetail::getDelFlag, IS_EXIST);
        List<String> exchangeNameList = exchangeDetailMapper.selectList(exchangeWrapper).stream().map(ExchangeDetail::getExchangeLabel).toList();
        realOfferDetailResp.setExchangeNameList(exchangeNameList);
        return Result.ok(realOfferDetailResp);
    }

    public Result<?> updateRealOfferBase(RealOfferBaseUpdateReq req) {
        RealOffer realOffer = new RealOffer();
        BeanUtils.copyProperties(req, realOffer);
        realOffer.setKLinePeriod(req.getKlinePeriod());
        realOffer.setKLineUnit(req.getKlineUnit());
        if (this.baseMapper.updateById(realOffer) == 1) {
            RealOffer realOffer1 = this.baseMapper.selectById(realOffer.getId());
            if (realOffer1.getStatus().equals("运行中")) {
                Server server = serverMapper.selectById(realOffer1.getServerId());
                String jsonStr = JSONUtil.toJsonStr(realOffer1);
                System.out.println(jsonStr);
                String key = AESUtil.getKey(realOffer1.getAppId());
                String encrypt = AESUtil.encryptCBC(jsonStr, key);
                System.out.println(encrypt);
                System.out.println(String.valueOf(jsonStr.length()));
//                String reqUrl = "http://"+qntRobotIp+":"+qntRobotPort+"/api/v1/strategyRefresh";
                String reqUrl = "http://" + server.getServerIp() + ":" + realOffer1.getRealOfferPort() + "/api/v1/strategyRefresh";
                System.out.println("=======================发送更新参数命令=============================");
                HttpResponse response = HttpRequest
                        .post(reqUrl)
                        .body(encrypt)
                        .header(Header.CONTENT_TYPE, "application/json")
                        .header("Data-Length", String.valueOf(jsonStr.length()))
                        .timeout(60 * 1000)
                        .execute();
                System.out.println(response);
                if (response.isOk()) {
                    String result = response.body();
                    QntRobotResponse qntRobotResponse = JSONUtil.toBean(result, QntRobotResponse.class);
                    if (qntRobotResponse.getSuccess()) {
                        return Result.ok();
                    } else {
                        return Result.error(qntRobotResponse.getMsg(), null);
                    }
                } else {
                    return Result.error("更新失败", null);
                }
            }
        } else {
            return Result.error("更新失败", null);
        }
        return Result.ok();
    }

    public Result<?> updateRealOfferAutoTrade(RealOfferBaseAutoTradeUpdateReq req) {
        RealOffer realOffer = new RealOffer();
        BeanUtils.copyProperties(req, realOffer);
        if (this.baseMapper.updateById(realOffer) == 1) {
            RealOffer realOffer1 = this.baseMapper.selectById(realOffer.getId());
            if (realOffer1.getStatus().equals("运行中")) {
                Server server = serverMapper.selectById(realOffer1.getServerId());
                String jsonStr = JSONUtil.toJsonStr(realOffer1);
                String key = AESUtil.getKey(realOffer1.getAppId());
                String encrypt = AESUtil.encryptCBC(jsonStr, key);
                String reqUrl = "http://" + server.getServerIp() + ":" + realOffer1.getRealOfferPort() + "/api/v1/strategyRefresh";
                System.out.println("=======================发送更新参数命令=============================");
                HttpResponse response = HttpRequest
                        .post(reqUrl)
                        .body(encrypt)
                        .header(Header.CONTENT_TYPE, "application/json")
                        .header("Data-Length", String.valueOf(jsonStr.length()))
                        .timeout(60 * 1000)
                        .execute();
                System.out.println(response);
                if (response.isOk()) {
                    String result = response.body();
                    QntRobotResponse qntRobotResponse = JSONUtil.toBean(result, QntRobotResponse.class);
                    if (qntRobotResponse.getSuccess()) {
                        return Result.ok();
                    } else {
                        return Result.error(qntRobotResponse.getMsg(), null);
                    }
                } else {
                    return Result.error("更新失败", null);
                }
            }
        } else {
            return Result.error("更新失败", null);
        }
        return Result.ok();
    }

    public Result<?> updateRealOfferTradeParam(RealOfferTradeParamUpdateReq req) {
        RealOffer realOffer = new RealOffer();
        BeanUtils.copyProperties(req, realOffer);
        if (this.baseMapper.updateById(realOffer) == 1) {
            RealOffer realOffer1 = this.baseMapper.selectById(realOffer.getId());
            if (realOffer1.getStatus().equals("运行中")) {
                Server server = serverMapper.selectById(realOffer1.getServerId());
                String jsonStr = JSONUtil.toJsonStr(realOffer1);
                String key = AESUtil.getKey(realOffer1.getAppId());
                String encrypt = AESUtil.encryptCBC(jsonStr, key);
                String reqUrl = "http://" + server.getServerIp() + ":" + realOffer1.getRealOfferPort() + "/api/v1/strategyRefresh";
                System.out.println("=======================发送更新参数命令=============================");
                HttpResponse response = HttpRequest
                        .post(reqUrl)
                        .body(encrypt)
                        .header(Header.CONTENT_TYPE, "application/json")
                        .header("Data-Length", String.valueOf(jsonStr.length()))
                        .timeout(60 * 1000)
                        .execute();
                System.out.println(response);
                if (response.isOk()) {
                    String result = response.body();
                    QntRobotResponse qntRobotResponse = JSONUtil.toBean(result, QntRobotResponse.class);
                    if (qntRobotResponse.getSuccess()) {
                        return Result.ok();
                    } else {
                        return Result.error(qntRobotResponse.getMsg(), null);
                    }
                } else {
                    return Result.error("更新失败", null);
                }
            }
        } else {
            return Result.error("更新失败", null);
        }
        return Result.ok();
    }

    public Result<?> getAllExchange(Long id) {
        boolean warningFlag = false;
        boolean mexcFlag = false;
        boolean gateFlag = false;
        boolean kucoinFlag = false;
        boolean htxFlag = false;
        boolean bitgetFlag = false;
        boolean okxFlag = false;
        boolean bybitFlag = false;
        boolean coinexFlag = false;
        boolean bitmartFlag = false;
        Symbol symbol = symbolMapper.selectById(id);
        if (symbol.getExMexc() == -1) {
            warningFlag = true;
            mexcFlag = true;
        }
        if (symbol.getExGate() == -1) {
            warningFlag = true;
            gateFlag = true;
        }
        if (symbol.getExKucoin() == -1) {
            warningFlag = true;
            kucoinFlag = true;
        }
        if (symbol.getExHtx() == -1) {
            warningFlag = true;
            htxFlag = true;
        }
        if (symbol.getExBitget() == -1) {
            warningFlag = true;
            bitgetFlag = true;
        }
        if (symbol.getExOkx() == -1) {
            warningFlag = true;
            okxFlag = true;
        }
        if (symbol.getExBybit() == -1) {
            warningFlag = true;
            bybitFlag = true;
        }
        if (symbol.getExCoinex() == -1) {
            warningFlag = true;
            coinexFlag = true;
        }
        if (symbol.getExBitmart() == -1) {
            warningFlag = true;
            bitmartFlag = true;
        }
        final LambdaQueryWrapper<Exchange> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Exchange::getDelFlag, IS_EXIST);
        final boolean finalWarningFlag = warningFlag;
        final boolean finalMexcFlag = mexcFlag;
        final boolean finalOkxFlag = okxFlag;
        final boolean finalBybitFlag = bybitFlag;
        final boolean finalHtxFlag = htxFlag;
        final boolean finalBitgetFlag = bitgetFlag;
        final boolean finalKucoinFlag = kucoinFlag;
        final boolean finalGateFlag = gateFlag;
        final boolean finalCoinexFlag = coinexFlag;
        final boolean finalBitmartFlag = bitmartFlag;
        List<ExchangeResp> exchangeResps = exchangeMapper.selectList(wrapper).stream().map(record -> {
            ExchangeResp exchangeResp = new ExchangeResp();
            BeanUtils.copyProperties(record, exchangeResp);
            exchangeResp.setStatus(0);
            if (finalWarningFlag) {
                switch (record.getExchange()) {
                    case "okx":
                        if (finalOkxFlag) {
                            exchangeResp.setStatus(1);
                        }
                        break;
                    case "bybit":
                        if (finalBybitFlag) {
                            exchangeResp.setStatus(1);
                        }
                        break;
                    case "htx":
                        if (finalHtxFlag) {
                            exchangeResp.setStatus(1);
                        }
                        break;
                    case "bitget":
                        if (finalBitgetFlag) {
                            exchangeResp.setStatus(1);
                        }
                        break;
                    case "mexc":
                        if (finalMexcFlag) {
                            exchangeResp.setStatus(1);
                        }
                        break;
                    case "kucoin":
                        if (finalKucoinFlag) {
                            exchangeResp.setStatus(1);
                        }
                        break;
                    case "gate":
                        if (finalGateFlag) {
                            exchangeResp.setStatus(1);
                        }
                        break;
                    case "coinex":
                        if (finalCoinexFlag) {
                            exchangeResp.setStatus(1);
                        }
                        break;
                    case "bitmart":
                        if (finalBitmartFlag) {
                            exchangeResp.setStatus(1);
                        }
                        break;
                }
            }
            return exchangeResp;
        }).toList();
        return Result.ok(exchangeResps);
    }

    public Result<?> getAllSymbolList() {
        final LambdaQueryWrapper<Symbol> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Symbol::getDelFlag, IS_EXIST);
        List<Symbol> symbols = symbolMapper.selectList(wrapper);
        if (CollectionUtils.isNotEmpty(symbols)) {
            List<SymbolSelectResp> symbolSelectResps = symbols.stream().map(record -> {
                SymbolSelectResp symbolSelectResp = new SymbolSelectResp();
                symbolSelectResp.setLabel(record.getName().split("-")[0]);
                symbolSelectResp.setValue(record.getId());
                symbolSelectResp.setStatus(0);
                if (record.getExMexc() == -1 || record.getExGate() == -1 || record.getExKucoin() == -1 || record.getExHtx() == -1 || record.getExBitget() == -1 || record.getExOkx() == -1 || record.getExBybit() == -1 || record.getExCoinex() == -1 || record.getExBitmart() == -1) {
                    symbolSelectResp.setStatus(1);
                }
                return symbolSelectResp;
            }).toList();
            return Result.ok(symbolSelectResps);
        }
        return Result.ok();
    }

    public Result<?> getRealOfferSymbolList(Long id) {
        if (null == id) {
            Result.error("参数错误", null);
        }
        List<Long> respList = new ArrayList<>();
        final LambdaQueryWrapper<RealOfferSymbol> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RealOfferSymbol::getDelFlag, IS_EXIST);
        wrapper.eq(RealOfferSymbol::getRealOfferId, id);
        respList = realOfferSymbolMapper.selectList(wrapper).stream().map(RealOfferSymbol::getSymbolId).toList();
        return Result.ok(respList);
    }

    public Result<?> getCurrentSymbolExchangeList(Long symbolId, Long realOfferId) {
        if (null == symbolId || null == realOfferId) {
            Result.error("参数错误", null);
        }
        List<Long> respList = new ArrayList<>();
        final LambdaQueryWrapper<RealOfferSymbol> realOfferSymbolWrapper = new LambdaQueryWrapper<>();
        realOfferSymbolWrapper.eq(RealOfferSymbol::getSymbolId, symbolId);
        realOfferSymbolWrapper.eq(RealOfferSymbol::getDelFlag, IS_EXIST);
        List<RealOfferSymbol> realOfferSymbols = realOfferSymbolMapper.selectList(realOfferSymbolWrapper);
        if (CollectionUtils.isEmpty(realOfferSymbols)) {
            Symbol symbol = symbolMapper.selectById(symbolId);
            if (symbol.getExMexc() == 1) {
                final LambdaQueryWrapper<Exchange> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Exchange::getExchange, "mexc");
                respList.add(exchangeMapper.selectList(wrapper).get(0).getId());
            }
            if (symbol.getExGate() == 1) {
                final LambdaQueryWrapper<Exchange> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Exchange::getExchange, "gate");
                respList.add(exchangeMapper.selectList(wrapper).get(0).getId());
            }
            if (symbol.getExKucoin() == 1) {
                final LambdaQueryWrapper<Exchange> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Exchange::getExchange, "kucoin");
                respList.add(exchangeMapper.selectList(wrapper).get(0).getId());
            }
            if (symbol.getExHtx() == 1) {
                final LambdaQueryWrapper<Exchange> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Exchange::getExchange, "htx");
                respList.add(exchangeMapper.selectList(wrapper).get(0).getId());
            }
            if (symbol.getExBitget() == 1) {
                final LambdaQueryWrapper<Exchange> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Exchange::getExchange, "bitget");
                respList.add(exchangeMapper.selectList(wrapper).get(0).getId());
            }
            if (symbol.getExOkx() == 1) {
                final LambdaQueryWrapper<Exchange> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Exchange::getExchange, "okx");
                respList.add(exchangeMapper.selectList(wrapper).get(0).getId());
            }
            if (symbol.getExBybit() == 1) {
                final LambdaQueryWrapper<Exchange> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Exchange::getExchange, "bybit");
                respList.add(exchangeMapper.selectList(wrapper).get(0).getId());
            }
            if (symbol.getExCoinex() == 1) {
                final LambdaQueryWrapper<Exchange> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Exchange::getExchange, "coinex");
                respList.add(exchangeMapper.selectList(wrapper).get(0).getId());
            }
            if (symbol.getExBitmart() == 1) {
                final LambdaQueryWrapper<Exchange> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Exchange::getExchange, "bitmart");
                respList.add(exchangeMapper.selectList(wrapper).get(0).getId());
            }
            return Result.ok(respList);
        } else {
            final LambdaQueryWrapper<RealOfferSymbolExchange> realOfferSymbolExchangeWrapper = new LambdaQueryWrapper<>();
            realOfferSymbolExchangeWrapper.eq(RealOfferSymbolExchange::getRealOfferSymbolId, realOfferSymbols.get(0).getId());
            realOfferSymbolExchangeWrapper.eq(RealOfferSymbolExchange::getDelFlag, IS_EXIST);
            List<RealOfferSymbolExchange> realOfferSymbolExchanges = realOfferSymbolExchangeMapper.selectList(realOfferSymbolExchangeWrapper);
            if (CollectionUtils.isNotEmpty(realOfferSymbolExchanges)) {
                for (RealOfferSymbolExchange realOfferSymbolExchange : realOfferSymbolExchanges) {
                    final LambdaQueryWrapper<Exchange> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(Exchange::getExchange, realOfferSymbolExchange.getExchange());
                    respList.add(exchangeMapper.selectOne(wrapper).getId());
                }
            }
            return Result.ok(respList);
        }

    }

    public Result<?> updateSymbolExchange(SymbolExchangeUpdataReq req) {
        Date now = new Date();
        Symbol symbol = symbolMapper.selectById(req.getCurrentSymboleId());
        RealOffer realOffer = this.baseMapper.selectById(req.getRealOfferId());
        Server server = serverMapper.selectById(realOffer.getServerId());
        final LambdaQueryWrapper<RealOfferSymbol> realOfferSymbolWrapper = new LambdaQueryWrapper<>();
        realOfferSymbolWrapper.eq(RealOfferSymbol::getRealOfferId, realOffer.getId());
        realOfferSymbolWrapper.eq(RealOfferSymbol::getSymbolId, symbol.getId());
        List<RealOfferSymbol> realOfferSymbols = realOfferSymbolMapper.selectList(realOfferSymbolWrapper);
        RealOfferSymbol pojo = new RealOfferSymbol();
        if (CollectionUtils.isEmpty(realOfferSymbols)) {
            // 新币 尚未添加
            pojo.setDelFlag(IS_EXIST);
            pojo.setRealOfferId(realOffer.getId());
            pojo.setSymbolId(symbol.getId());
            pojo.setSymbolName(symbol.getName().split("-")[0]);
            pojo.setSymbolStatus(6);
            pojo.setDelayTime("0");
            pojo.setConsumingTime("0");
            pojo.setPollingTime("0");
            realOfferSymbolMapper.insert(pojo);
        } else {
            pojo = realOfferSymbols.get(0);
        }
        List<Long> oldExchangeList = new ArrayList<>();
        final LambdaQueryWrapper<RealOfferSymbolExchange> oldWrapper = new LambdaQueryWrapper<>();
        oldWrapper.eq(RealOfferSymbolExchange::getDelFlag, IS_EXIST);
        oldWrapper.eq(RealOfferSymbolExchange::getRealOfferSymbolId, pojo.getId());
        List<String> stringList = realOfferSymbolExchangeMapper.selectList(oldWrapper).stream().map(RealOfferSymbolExchange::getExchange).toList();
        if (CollectionUtils.isNotEmpty(stringList)) {
            final LambdaQueryWrapper<Exchange> exchangeWrapper = new LambdaQueryWrapper<>();
            exchangeWrapper.in(Exchange::getExchange, stringList);
            oldExchangeList = exchangeMapper.selectList(exchangeWrapper).stream().map(Exchange::getId).toList();
        }
        List<Long> newExchangeList = new ArrayList<>();
        newExchangeList = req.getCurrentSymbolExchangeData();
        List<Long> addExchangeList = new ArrayList<>();
        List<Long> subExchangeList = new ArrayList<>();
        addExchangeList.addAll(newExchangeList);
        subExchangeList.addAll(oldExchangeList);
        addExchangeList.removeAll(oldExchangeList);
        subExchangeList.removeAll(newExchangeList);
        if (CollectionUtils.isNotEmpty(addExchangeList)) {
            for (Long addExchangeId : addExchangeList) {
                RealOfferSymbolExchange realOfferSymbolExchange = new RealOfferSymbolExchange();
                realOfferSymbolExchange.setRealOfferSymbolId(pojo.getId());
                realOfferSymbolExchange.setExchange(exchangeMapper.selectById(addExchangeId).getExchange());
                realOfferSymbolExchange.setStatus(1);
                realOfferSymbolExchangeMapper.insert(realOfferSymbolExchange);
            }
        }
        if (CollectionUtils.isNotEmpty(subExchangeList)) {
            for (Long subExchangeId : subExchangeList) {
                String exchange = exchangeMapper.selectById(subExchangeId).getExchange();
                final LambdaQueryWrapper<RealOfferSymbolExchange> subWrapper = new LambdaQueryWrapper<>();
                subWrapper.eq(RealOfferSymbolExchange::getRealOfferSymbolId, pojo.getId());
                subWrapper.eq(RealOfferSymbolExchange::getExchange, exchange);
                realOfferSymbolExchangeMapper.delete(subWrapper);
            }
        }

        if (CollectionUtils.isEmpty(realOfferSymbols)) {
            //调用初始化持仓接口
            Map<String, Object> reqForm = new HashMap<>();
            List<Map<String, String>> exchangeList = new ArrayList<>();
            reqForm.put("symbol", symbol.getName());
            reqForm.put("value", Double.valueOf(realOffer.getBasicWarehouseValue()));
            reqForm.put("pricePrecision", symbol.getPricePrecision());
            reqForm.put("amountPrecision", symbol.getAmountPrecision());
            exchangeList = getExchangeList(symbol, realOffer.getId());
            reqForm.put("exchanges", exchangeList);
            String jsonStr = JSONUtil.toJsonStr(reqForm);
//                String key = AESUtil.getKey(realOffer.getAppId());
//                String encrypt = AESUtil.encryptCBC(jsonStr, key);
            String reqUrl = "http://" + qntRobotIp + ":" + qntRobotPort + "/api/v1/initPos";
            HttpResponse response = HttpRequest.post(reqUrl).body(jsonStr).header(Header.CONTENT_TYPE, "application/json").header("Data-Length", String.valueOf(jsonStr.length())).timeout(60 * 1000).execute();
            System.out.println(response.body());
            String result = response.body();
            QntRobotResponse qntRobotResponse = JSONUtil.toBean(result, QntRobotResponse.class);
            if (!response.isOk() || !qntRobotResponse.getSuccess()) {
                realOfferSymbolMapper.deleteById(pojo);
                return Result.error("【" + symbol.getName() + "】 初始化失败", null);
            } else {
                pojo.setOperationTime(now);
                realOfferSymbolMapper.updateById(pojo);
            }
        } else {
            if (CollectionUtils.isNotEmpty(addExchangeList) || CollectionUtils.isNotEmpty(subExchangeList)) {
                //调用修改交易对接口
                if (realOffer.getStatus().equals("运行中")) {
                    // 调用修改交易对（页面币-编辑交易所）接口 成功后修改数据库
                    Map<String, Object> reqForm = new HashMap<>();
                    List<Map<String, Object>> exchangeList = new ArrayList<>();
                    final LambdaQueryWrapper<Exchange> wrapper = new LambdaQueryWrapper<>();
                    wrapper.in(Exchange::getId, req.getCurrentSymbolExchangeData());
                    exchangeList = getExchangeNameList(pojo, symbol, realOffer.getId());
                    reqForm.put("symbol", symbol.getName());
//                    reqForm.put("pricePrecision", symbol.getPricePrecision());
//                    reqForm.put("amountPrecision", symbol.getAmountPrecision());
                    reqForm.put("exchanges", exchangeList);
                    String jsonStr = JSONUtil.toJsonStr(reqForm);
                    String key = AESUtil.getKey(realOffer.getAppId());
                    String encrypt = AESUtil.encryptCBC(jsonStr, key);
                    String reqUrl = "http://" + server.getServerIp() + ":" + realOffer.getRealOfferPort() + "/api/v1/changeSymbol";
                    HttpResponse response = HttpRequest
                            .post(reqUrl)
                            .body(encrypt)
                            .header(Header.CONTENT_TYPE, "application/json")
                            .header("Data-Length", String.valueOf(jsonStr.length()))
                            .timeout(60 * 1000)
                            .execute();
                    System.out.println(response.body());
                }
            }
        }
        return Result.ok();
    }

    public Result<?> updateAddSymbol(AddCurrencyToRealOfferReq req) throws Exception {
        Date now = new Date();
        Long realOfferId = req.getId();
        RealOffer realOffer = this.baseMapper.selectById(realOfferId);
        List<Long> oldCurrencyList = new ArrayList<>();
        final LambdaQueryWrapper<RealOfferSymbol> oldWrapper = new LambdaQueryWrapper<>();
        oldWrapper.eq(RealOfferSymbol::getDelFlag, IS_EXIST);
        oldWrapper.eq(RealOfferSymbol::getRealOfferId, realOfferId);
        oldCurrencyList = realOfferSymbolMapper.selectList(oldWrapper).stream().map(RealOfferSymbol::getSymbolId).toList();
        List<Long> newCurrencyList = new ArrayList<>();
        newCurrencyList = req.getCurrencyListValue();
        List<Long> addCurrencyList = new ArrayList<>();
        List<Long> subCurrencyList = new ArrayList<>();
        addCurrencyList.addAll(newCurrencyList);
        subCurrencyList.addAll(oldCurrencyList);
        addCurrencyList.removeAll(oldCurrencyList);
        subCurrencyList.removeAll(newCurrencyList);
        List<String> errorSymbolNameList = new ArrayList<>();
        for (Long subId : subCurrencyList) {
            final LambdaQueryWrapper<RealOfferSymbol> checkSubWrapper = new LambdaQueryWrapper<>();
            checkSubWrapper.eq(RealOfferSymbol::getRealOfferId, realOfferId);
            checkSubWrapper.eq(RealOfferSymbol::getSymbolId, subId);
            checkSubWrapper.eq(RealOfferSymbol::getDelFlag, IS_EXIST);
            RealOfferSymbol realOfferSymbol = realOfferSymbolMapper.selectOne(checkSubWrapper);
            if (realOfferSymbol.getSymbolStatus() == 4 || realOfferSymbol.getSymbolStatus() == 6) {

            } else {
                errorSymbolNameList.add(realOfferSymbol.getSymbolName());
            }
        }
        if (CollectionUtils.isNotEmpty(errorSymbolNameList)) {
            StringBuilder errorInfo = new StringBuilder("【 ");
            for (String symbolName : errorSymbolNameList) {
                errorInfo.append(symbolName).append(" ");
            }
            errorInfo.append("】 尚未停止，请先停止再进行删除操作");
            return Result.error(errorInfo.toString(), null);
        }
        StringBuilder addErrorInfo = new StringBuilder();
        addErrorInfo.append("【 ");
        if (CollectionUtils.isNotEmpty(addCurrencyList)) {
            for (Long addCurrencyId : addCurrencyList) {
                Symbol symbol = symbolMapper.selectById(addCurrencyId);
                RealOfferSymbol addPojo = new RealOfferSymbol();
                addPojo.setDelFlag(IS_EXIST);
                addPojo.setRealOfferId(realOfferId);
                addPojo.setSymbolId(addCurrencyId);
                addPojo.setSymbolName(symbol.getName().split("-")[0]);
                addPojo.setSymbolStatus(5);
                addPojo.setDelayTime("0");
                addPojo.setConsumingTime("0");
                addPojo.setPollingTime("0");
                realOfferSymbolMapper.insert(addPojo);
                // 调用初始化持仓接口 成功后进行数据库操作
                Map<String, Object> reqForm = new HashMap<>();
                List<Map<String, String>> exchangeList = new ArrayList<>();
                reqForm.put("symbol", symbol.getName());
                reqForm.put("value", Double.valueOf(realOffer.getBasicWarehouseValue()));
                reqForm.put("pricePrecision", symbol.getPricePrecision());
                reqForm.put("amountPrecision", symbol.getAmountPrecision());
                exchangeList = getExchangeList(symbol, realOfferId);
                reqForm.put("exchanges", exchangeList);
                String jsonStr = JSONUtil.toJsonStr(reqForm);
//                String key = AESUtil.getKey(realOffer.getAppId());
//                String encrypt = AESUtil.encryptCBC(jsonStr, key);
                String reqUrl = "http://" + qntRobotIp + ":" + qntRobotPort + "/api/v1/initPos";
                System.out.println(jsonStr);
                System.out.println(jsonStr.length());
                HttpResponse response = HttpRequest.post(reqUrl).body(jsonStr).header(Header.CONTENT_TYPE, "application/json").header("Data-Length", String.valueOf(jsonStr.length())).timeout(60 * 1000).execute();
                System.out.println(response.body());
                if (response.isOk()) {
                    String result = response.body();
                    QntRobotResponse qntRobotResponse = JSONUtil.toBean(result, QntRobotResponse.class);
                    System.out.println(qntRobotResponse.getSuccess() + ":" + result);
                    if (qntRobotResponse.getSuccess()) {
                        addPojo.setSymbolStatus(6);
                        addPojo.setOperationTime(now);
                        realOfferSymbolMapper.updateById(addPojo);
                        // 添加币-交易所表信息
                        for (Map<String, String> exchange : exchangeList) {
                            RealOfferSymbolExchange realOfferSymbolExchange = new RealOfferSymbolExchange();
                            realOfferSymbolExchange.setRealOfferSymbolId(addPojo.getId());
                            realOfferSymbolExchange.setExchange(exchange.get("name"));
                            realOfferSymbolExchange.setStatus(1);
                            realOfferSymbolExchangeMapper.insert(realOfferSymbolExchange);
                        }
                    } else {
                        addErrorInfo.append(symbol.getName());
                        addErrorInfo.append(" ");
                    }
                } else {
                    addErrorInfo.append(symbol.getName());
                    addErrorInfo.append(" ");
                }
            }
        }
        StringBuilder subErrorInfo = new StringBuilder();
        subErrorInfo.append("【 ");
        if (CollectionUtils.isNotEmpty(subCurrencyList)) {
            for (Long subId : subCurrencyList) {
                Symbol symbol = symbolMapper.selectById(subId);
                // 调用初始化清仓接口
                Map<String, Object> reqForm = new HashMap<>();
                List<Map<String, String>> exchangeList = new ArrayList<>();
                reqForm.put("symbol", symbol.getName());
                reqForm.put("pricePrecision", symbol.getPricePrecision());
                reqForm.put("amountPrecision", symbol.getAmountPrecision());
                exchangeList = getExchangeList(symbol, realOfferId);
                reqForm.put("exchanges", exchangeList);
                String jsonStr = JSONUtil.toJsonStr(reqForm);
//                String key = AESUtil.getKey(realOffer.getAppId());
//                String encrypt = AESUtil.encryptCBC(jsonStr, key);
                String reqUrl = "http://" + qntRobotIp + ":" + qntRobotPort + "/api/v1/clearPos";
                HttpResponse response = HttpRequest.post(reqUrl).body(jsonStr).header(Header.CONTENT_TYPE, "application/json").header("Data-Length", String.valueOf(jsonStr.length())).timeout(60 * 1000).execute();
                System.out.println(response.body());
                if (response.isOk()) {
                    String result = response.body();
                    QntRobotResponse qntRobotResponse = JSONUtil.toBean(result, QntRobotResponse.class);
                    System.out.println(qntRobotResponse.getSuccess() + ":" + result);
                    if (qntRobotResponse.getSuccess()) {
                        // 清仓成功 删除币及币-交易所信息
                        final LambdaQueryWrapper<RealOfferSymbol> subWrapper = new LambdaQueryWrapper<>();
                        subWrapper.eq(RealOfferSymbol::getRealOfferId, realOfferId);
                        subWrapper.eq(RealOfferSymbol::getSymbolId, subId);
                        subWrapper.eq(RealOfferSymbol::getDelFlag, IS_EXIST);
                        RealOfferSymbol realOfferSymbol = realOfferSymbolMapper.selectOne(subWrapper);
                        final LambdaQueryWrapper<RealOfferSymbolExchange> subExWrapper = new LambdaQueryWrapper<>();
                        subExWrapper.eq(RealOfferSymbolExchange::getRealOfferSymbolId, realOfferSymbol.getId());
                        realOfferSymbolExchangeMapper.delete(subExWrapper);
                        realOfferSymbolMapper.deleteById(realOfferSymbol);
                    } else {
                        subErrorInfo.append(symbol.getName());
                        subErrorInfo.append(" ");
                    }
                } else {
                    subErrorInfo.append(symbol.getName());
                    subErrorInfo.append(" ");
                }
            }
        }
        System.out.println("======================" + addErrorInfo.toString() + "==========================");
        System.out.println("======================" + subErrorInfo.toString() + "==========================");
        if (addErrorInfo.toString().length() > 2) {
            addErrorInfo.append("】");
        }
        if (subErrorInfo.toString().length() > 2) {
            subErrorInfo.append("】");
        }
        if (addErrorInfo.toString().length() > 2 && subErrorInfo.toString().length() > 2) {
            addErrorInfo.append(subErrorInfo);
            return Result.error(addErrorInfo.toString(), null);
        }
        if (addErrorInfo.toString().length() > 2 && subErrorInfo.toString().length() <= 2) {
            return Result.error(addErrorInfo.toString(), null);
        }
        if (addErrorInfo.toString().length() <= 2 && subErrorInfo.toString().length() > 2) {
            return Result.error(subErrorInfo.toString(), null);
        }
        return Result.ok();
    }

    public List<Map<String, Object>> getExchangeNameList(RealOfferSymbol realOfferSymbol, Symbol symbol, Long realOfferId) {
        List<Map<String, Object>> exchangeList = new ArrayList<>();
        final LambdaQueryWrapper<RealOfferSymbolExchange> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RealOfferSymbolExchange::getRealOfferSymbolId, realOfferSymbol.getId());
        exchangeList = realOfferSymbolExchangeMapper.selectList(wrapper).stream().map(record -> {
            Map<String, Object> exchageInfo = new HashMap<>();
            exchageInfo.put("name", record.getExchange());
            if (record.getExchange().equals("mexc")) {
                exchageInfo.put("pricePrecision", symbol.getExMexcPrice());
                exchageInfo.put("amountPrecision", symbol.getExMexcAmount());
            }
            if (record.getExchange().equals("gate")) {
                exchageInfo.put("pricePrecision", symbol.getExGatePrice());
                exchageInfo.put("amountPrecision", symbol.getExGateAmount());
            }
            if (record.getExchange().equals("kucoin")) {
                exchageInfo.put("pricePrecision", symbol.getExKucoinPrice());
                exchageInfo.put("amountPrecision", symbol.getExKucoinAmount());
            }
            if (record.getExchange().equals("htx")) {
                exchageInfo.put("pricePrecision", symbol.getExHtxPrice());
                exchageInfo.put("amountPrecision", symbol.getExHtxAmount());
            }
            if (record.getExchange().equals("bitget")) {
                exchageInfo.put("pricePrecision", symbol.getExBitgetPrice());
                exchageInfo.put("amountPrecision", symbol.getExBitgetAmount());
            }
            if (record.getExchange().equals("okx")) {
                exchageInfo.put("pricePrecision", symbol.getExOkxPrice());
                exchageInfo.put("amountPrecision", symbol.getExOkxAmount());
            }
            if (record.getExchange().equals("bybit")) {
                exchageInfo.put("pricePrecision", symbol.getExBybitPrice());
                exchageInfo.put("amountPrecision", symbol.getExBybitAmount());
            }
            if (record.getExchange().equals("coinex")) {
                exchageInfo.put("pricePrecision", symbol.getExCoinexPrice());
                exchageInfo.put("amountPrecision", symbol.getExCoinexAmount());
            }
            if (record.getExchange().equals("bitmart")) {
                exchageInfo.put("pricePrecision", symbol.getExBitmartPrice());
                exchageInfo.put("amountPrecision", symbol.getExBitmartAmount());
            }
            return exchageInfo;
        }).toList();
        return exchangeList;
    }

    public List<String> getExchangeNameList(RealOfferSymbol realOfferSymbol, Long realOfferId) {
        List<String> exchangeList = new ArrayList<>();
        final LambdaQueryWrapper<RealOfferSymbolExchange> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RealOfferSymbolExchange::getRealOfferSymbolId, realOfferSymbol.getId());
        exchangeList = realOfferSymbolExchangeMapper.selectList(wrapper).stream().map(RealOfferSymbolExchange::getExchange).toList();
        return exchangeList;
    }

    public List<Map<String, String>> getExchangeList(Symbol symbol, Long realOfferId) {
        List<Map<String, String>> exchangeList = new ArrayList<>();
        final LambdaQueryWrapper<RealOfferSymbol> realOfferSymbolWrapper = new LambdaQueryWrapper<>();
        realOfferSymbolWrapper.eq(RealOfferSymbol::getRealOfferId, realOfferId);
        realOfferSymbolWrapper.eq(RealOfferSymbol::getSymbolId, symbol.getId());
        realOfferSymbolWrapper.eq(RealOfferSymbol::getDelFlag, IS_EXIST);
        List<RealOfferSymbol> realOfferSymbols = realOfferSymbolMapper.selectList(realOfferSymbolWrapper);
        final LambdaQueryWrapper<RealOfferSymbolExchange> realOfferSymbolExchangeWrapper = new LambdaQueryWrapper<>();
        realOfferSymbolExchangeWrapper.eq(RealOfferSymbolExchange::getRealOfferSymbolId, realOfferSymbols.get(0).getId());
        realOfferSymbolExchangeWrapper.eq(RealOfferSymbolExchange::getDelFlag, IS_EXIST);
        List<RealOfferSymbolExchange> realOfferSymbolExchanges1 = realOfferSymbolExchangeMapper.selectList(realOfferSymbolExchangeWrapper);
        if (CollectionUtils.isEmpty(realOfferSymbolExchanges1)) {
            if (symbol.getExMexc() == 1) {
                final LambdaQueryWrapper<RealOfferExchange> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(RealOfferExchange::getRealOfferId, realOfferId);
                wrapper.eq(RealOfferExchange::getExchange, "mexc");
                List<RealOfferExchange> realOfferExchange = realOfferExchangeMapper.selectList(wrapper);
                if (CollectionUtils.isNotEmpty(realOfferExchange)) {
                    Map<String, String> exchangeInfo = new HashMap<>();
                    ExchangeDetail exchangeDetail = exchangeDetailMapper.selectById(realOfferExchange.get(0).getExchangeId());
                    exchangeInfo.put("name", "mexc");
                    exchangeInfo.put("apiKey", exchangeDetail.getAccessKey());
                    exchangeInfo.put("apiSecret", exchangeDetail.getSecretKey());
                    exchangeInfo.put("apiPassphrase", exchangeDetail.getPassphrase());
                    exchangeList.add(exchangeInfo);
                }
            }
            if (symbol.getExGate() == 1) {
                final LambdaQueryWrapper<RealOfferExchange> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(RealOfferExchange::getRealOfferId, realOfferId);
                wrapper.eq(RealOfferExchange::getExchange, "gate");
                List<RealOfferExchange> realOfferExchange = realOfferExchangeMapper.selectList(wrapper);
                if (CollectionUtils.isNotEmpty(realOfferExchange)) {
                    Map<String, String> exchangeInfo = new HashMap<>();
                    ExchangeDetail exchangeDetail = exchangeDetailMapper.selectById(realOfferExchange.get(0).getExchangeId());
                    exchangeInfo.put("name", "gate");
                    exchangeInfo.put("apiKey", exchangeDetail.getAccessKey());
                    exchangeInfo.put("apiSecret", exchangeDetail.getSecretKey());
                    exchangeInfo.put("apiPassphrase", exchangeDetail.getPassphrase());
                    exchangeList.add(exchangeInfo);
                }
            }
            if (symbol.getExKucoin() == 1) {
                final LambdaQueryWrapper<RealOfferExchange> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(RealOfferExchange::getRealOfferId, realOfferId);
                wrapper.eq(RealOfferExchange::getExchange, "kucoin");
                List<RealOfferExchange> realOfferExchange = realOfferExchangeMapper.selectList(wrapper);
                if (CollectionUtils.isNotEmpty(realOfferExchange)) {
                    Map<String, String> exchangeInfo = new HashMap<>();
                    ExchangeDetail exchangeDetail = exchangeDetailMapper.selectById(realOfferExchange.get(0).getExchangeId());
                    exchangeInfo.put("name", "kucoin");
                    exchangeInfo.put("apiKey", exchangeDetail.getAccessKey());
                    exchangeInfo.put("apiSecret", exchangeDetail.getSecretKey());
                    exchangeInfo.put("apiPassphrase", exchangeDetail.getPassword());
                    exchangeList.add(exchangeInfo);
                }
            }
            if (symbol.getExHtx() == 1) {
                final LambdaQueryWrapper<RealOfferExchange> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(RealOfferExchange::getRealOfferId, realOfferId);
                wrapper.eq(RealOfferExchange::getExchange, "htx");
                List<RealOfferExchange> realOfferExchange = realOfferExchangeMapper.selectList(wrapper);
                if (CollectionUtils.isNotEmpty(realOfferExchange)) {
                    Map<String, String> exchangeInfo = new HashMap<>();
                    ExchangeDetail exchangeDetail = exchangeDetailMapper.selectById(realOfferExchange.get(0).getExchangeId());
                    exchangeInfo.put("name", "htx");
                    exchangeInfo.put("apiKey", exchangeDetail.getAccessKey());
                    exchangeInfo.put("apiSecret", exchangeDetail.getSecretKey());
                    exchangeInfo.put("apiPassphrase", exchangeDetail.getPassphrase());
                    exchangeList.add(exchangeInfo);
                }
            }
            if (symbol.getExBitget() == 1) {
                final LambdaQueryWrapper<RealOfferExchange> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(RealOfferExchange::getRealOfferId, realOfferId);
                wrapper.eq(RealOfferExchange::getExchange, "bitget");
                List<RealOfferExchange> realOfferExchange = realOfferExchangeMapper.selectList(wrapper);
                if (CollectionUtils.isNotEmpty(realOfferExchange)) {
                    Map<String, String> exchangeInfo = new HashMap<>();
                    ExchangeDetail exchangeDetail = exchangeDetailMapper.selectById(realOfferExchange.get(0).getExchangeId());
                    exchangeInfo.put("name", "bitget");
                    exchangeInfo.put("apiKey", exchangeDetail.getAccessKey());
                    exchangeInfo.put("apiSecret", exchangeDetail.getSecretKey());
                    exchangeInfo.put("apiPassphrase", exchangeDetail.getPassphrase());
                    exchangeList.add(exchangeInfo);
                }
            }
            if (symbol.getExOkx() == 1) {
                final LambdaQueryWrapper<RealOfferExchange> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(RealOfferExchange::getRealOfferId, realOfferId);
                wrapper.eq(RealOfferExchange::getExchange, "okx");
                List<RealOfferExchange> realOfferExchange = realOfferExchangeMapper.selectList(wrapper);
                if (CollectionUtils.isNotEmpty(realOfferExchange)) {
                    Map<String, String> exchangeInfo = new HashMap<>();
                    ExchangeDetail exchangeDetail = exchangeDetailMapper.selectById(realOfferExchange.get(0).getExchangeId());
                    exchangeInfo.put("name", "okx");
                    exchangeInfo.put("apiKey", exchangeDetail.getAccessKey());
                    exchangeInfo.put("apiSecret", exchangeDetail.getSecretKey());
                    exchangeInfo.put("apiPassphrase", exchangeDetail.getPassphrase());
                    exchangeList.add(exchangeInfo);
                }
            }
            if (symbol.getExBybit() == 1) {
                final LambdaQueryWrapper<RealOfferExchange> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(RealOfferExchange::getRealOfferId, realOfferId);
                wrapper.eq(RealOfferExchange::getExchange, "bybit");
                List<RealOfferExchange> realOfferExchange = realOfferExchangeMapper.selectList(wrapper);
                if (CollectionUtils.isNotEmpty(realOfferExchange)) {
                    Map<String, String> exchangeInfo = new HashMap<>();
                    ExchangeDetail exchangeDetail = exchangeDetailMapper.selectById(realOfferExchange.get(0).getExchangeId());
                    exchangeInfo.put("name", "bybit");
                    exchangeInfo.put("apiKey", exchangeDetail.getAccessKey());
                    exchangeInfo.put("apiSecret", exchangeDetail.getSecretKey());
                    exchangeInfo.put("apiPassphrase", exchangeDetail.getPassphrase());
                    exchangeList.add(exchangeInfo);
                }
            }
            if (symbol.getExCoinex() == 1) {
                final LambdaQueryWrapper<RealOfferExchange> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(RealOfferExchange::getRealOfferId, realOfferId);
                wrapper.eq(RealOfferExchange::getExchange, "coinex");
                List<RealOfferExchange> realOfferExchange = realOfferExchangeMapper.selectList(wrapper);
                if (CollectionUtils.isNotEmpty(realOfferExchange)) {
                    Map<String, String> exchangeInfo = new HashMap<>();
                    ExchangeDetail exchangeDetail = exchangeDetailMapper.selectById(realOfferExchange.get(0).getExchangeId());
                    exchangeInfo.put("name", "coinex");
                    exchangeInfo.put("apiKey", exchangeDetail.getAccessKey());
                    exchangeInfo.put("apiSecret", exchangeDetail.getSecretKey());
                    exchangeInfo.put("apiPassphrase", exchangeDetail.getPassphrase());
                    exchangeList.add(exchangeInfo);
                }
            }
            if (symbol.getExBitmart() == 1) {
                final LambdaQueryWrapper<RealOfferExchange> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(RealOfferExchange::getRealOfferId, realOfferId);
                wrapper.eq(RealOfferExchange::getExchange, "bitmart");
                List<RealOfferExchange> realOfferExchange = realOfferExchangeMapper.selectList(wrapper);
                if (CollectionUtils.isNotEmpty(realOfferExchange)) {
                    Map<String, String> exchangeInfo = new HashMap<>();
                    ExchangeDetail exchangeDetail = exchangeDetailMapper.selectById(realOfferExchange.get(0).getExchangeId());
                    exchangeInfo.put("name", "bitmart");
                    exchangeInfo.put("apiKey", exchangeDetail.getAccessKey());
                    exchangeInfo.put("apiSecret", exchangeDetail.getSecretKey());
                    exchangeInfo.put("apiPassphrase", exchangeDetail.getPassphrase());
                    exchangeList.add(exchangeInfo);
                }
            }
        } else {
            final LambdaQueryWrapper<RealOfferSymbolExchange> realOfferSymbolExchangeWrapper1 = new LambdaQueryWrapper<>();
            realOfferSymbolExchangeWrapper1.eq(RealOfferSymbolExchange::getRealOfferSymbolId, realOfferSymbols.get(0).getId());
            List<RealOfferSymbolExchange> realOfferSymbolExchanges = realOfferSymbolExchangeMapper.selectList(realOfferSymbolExchangeWrapper1);
            for (RealOfferSymbolExchange realOfferSymbolExchange : realOfferSymbolExchanges) {
                Map<String, String> exchangeInfo = new HashMap<>();
                final LambdaQueryWrapper<RealOfferExchange> realOfferExchangeWrapper = new LambdaQueryWrapper<>();
                realOfferExchangeWrapper.eq(RealOfferExchange::getExchange, realOfferSymbolExchange.getExchange());
                realOfferExchangeWrapper.eq(RealOfferExchange::getRealOfferId, realOfferId);
                Long id = realOfferExchangeMapper.selectList(realOfferExchangeWrapper).get(0).getExchangeId();
                ExchangeDetail exchangeDetail = exchangeDetailMapper.selectById(id);
                if (realOfferSymbolExchange.getExchange().equals("kucoin")) {
                    exchangeInfo.put("name", realOfferSymbolExchange.getExchange());
                    exchangeInfo.put("apiKey", exchangeDetail.getAccessKey());
                    exchangeInfo.put("apiSecret", exchangeDetail.getSecretKey());
                    exchangeInfo.put("apiPassphrase", exchangeDetail.getPassword());
                    exchangeList.add(exchangeInfo);
                } else {
                    exchangeInfo.put("name", realOfferSymbolExchange.getExchange());
                    exchangeInfo.put("apiKey", exchangeDetail.getAccessKey());
                    exchangeInfo.put("apiSecret", exchangeDetail.getSecretKey());
                    exchangeInfo.put("apiPassphrase", exchangeDetail.getPassphrase());
                    exchangeList.add(exchangeInfo);
                }

            }
        }


        return exchangeList;
    }

    public Result<?> getRealOfferAllSymbol(Long id) {
        if (null == id) {
            Result.error("参数错误", null);
        }
        final LambdaQueryWrapper<RealOfferSymbol> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RealOfferSymbol::getDelFlag, IS_EXIST);
        wrapper.eq(RealOfferSymbol::getRealOfferId, id);
        List<SymbolSelectResp> respList = realOfferSymbolMapper.selectList(wrapper).stream().map(record -> {
            SymbolSelectResp symbolSelectResp = new SymbolSelectResp();
            symbolSelectResp.setLabel(record.getSymbolName());
            symbolSelectResp.setValue(record.getId());
            return symbolSelectResp;
        }).toList();
        return Result.ok(respList);
    }

    public Result<?> getRealOfferStartingSymbol(Long id) {
        if (null == id) {
            Result.error("参数错误", null);
        }
        List<Long> respList = new ArrayList<>();
        final LambdaQueryWrapper<RealOfferSymbol> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RealOfferSymbol::getDelFlag, IS_EXIST);
        wrapper.eq(RealOfferSymbol::getRealOfferId, id);
        wrapper.eq(RealOfferSymbol::getSymbolStatus, 1);
        respList = realOfferSymbolMapper.selectList(wrapper).stream().map(RealOfferSymbol::getId).toList();
        return Result.ok(respList);
    }

    public Result<?> updateStartSymbol(AddCurrencyToRealOfferReq req) throws Exception {
        Date now = new Date();
        StringBuilder errorResp = new StringBuilder();
        StringBuilder addErrorInfo = new StringBuilder();
        addErrorInfo.append("【 ");
        StringBuilder addErrorInfo2 = new StringBuilder();
        addErrorInfo2.append("【 ");
        StringBuilder subErrorInfo = new StringBuilder();
        subErrorInfo.append("【 ");
        StringBuilder subErrorInfo2 = new StringBuilder();
        subErrorInfo2.append("【 ");
        Long realOfferId = req.getId();
        RealOffer realOffer = this.baseMapper.selectById(realOfferId);
        Server server = serverMapper.selectById(realOffer.getServerId());
        List<Long> oldCurrencyList = new ArrayList<>();
        final LambdaQueryWrapper<RealOfferSymbol> oldWrapper = new LambdaQueryWrapper<>();
        oldWrapper.eq(RealOfferSymbol::getDelFlag, IS_EXIST);
        oldWrapper.eq(RealOfferSymbol::getRealOfferId, realOfferId);
        oldWrapper.eq(RealOfferSymbol::getSymbolStatus, 1);
        List<RealOfferSymbol> realOfferSymbolList = realOfferSymbolMapper.selectList(oldWrapper);
        oldCurrencyList = realOfferSymbolList.stream().map(RealOfferSymbol::getId).toList();
        List<Long> newCurrencyList = new ArrayList<>();
        newCurrencyList = req.getCurrencyListValue();
        List<Long> addCurrencyList = new ArrayList<>();
        List<Long> subCurrencyList = new ArrayList<>();
        addCurrencyList.addAll(newCurrencyList);
        subCurrencyList.addAll(oldCurrencyList);
        addCurrencyList.removeAll(oldCurrencyList);
        subCurrencyList.removeAll(newCurrencyList);
        if (CollectionUtils.isNotEmpty(addCurrencyList)) {
            for (Long addCurrencyId : addCurrencyList) {
                RealOfferSymbol realOfferSymbol = realOfferSymbolMapper.selectById(addCurrencyId);
                if (null != realOfferSymbol && (realOfferSymbol.getSymbolStatus() != 5 || realOfferSymbol.getSymbolStatus() != 2)) {
                    RealOfferSymbol addPojo = new RealOfferSymbol();
                    addPojo.setId(addCurrencyId);
                    addPojo.setSymbolStatus(1);
                    addPojo.setOperationTime(now);
                    if (realOffer.getStatus().equals("运行中")) {
                        // 调用启动接口（添加交易对） 成功后进行数据库操作
                        Symbol symbol = symbolMapper.selectById(realOfferSymbol.getSymbolId());
                        Map<String, Object> reqForm = new HashMap<>();
                        List<Map<String, Object>> exchangeList = new ArrayList<>();
                        reqForm.put("symbol", symbol.getName());
//                        reqForm.put("pricePrecision", symbol.getPricePrecision());
//                        reqForm.put("amountPrecision", symbol.getAmountPrecision());
                        exchangeList = getExchangeNameList(realOfferSymbol, symbol, realOfferId);
                        reqForm.put("exchanges", exchangeList);
                        String jsonStr = JSONUtil.toJsonStr(reqForm);
                        String key = AESUtil.getKey(realOffer.getAppId());
                        String encrypt = AESUtil.encryptCBC(jsonStr, key);
//                        String reqUrl = "http://"+qntRobotIp+":"+qntRobotPort+"/api/v1/newSymbol";
                        String reqUrl = "http://" + server.getServerIp() + ":" + realOffer.getRealOfferPort() + "/api/v1/newSymbol";
                        HttpResponse response = HttpRequest
                                .post(reqUrl)
                                .body(encrypt)
                                .header(Header.CONTENT_TYPE, "application/json")
                                .header("Data-Length", String.valueOf(jsonStr.length()))
                                .timeout(60 * 1000)
                                .execute();
                        System.out.println(response.body());
                        if (response.isOk()) {
                            String result = response.body();
                            QntRobotResponse qntRobotResponse = JSONUtil.toBean(result, QntRobotResponse.class);
                            System.out.println(qntRobotResponse.getSuccess() + ":" + result);
                            if (qntRobotResponse.getSuccess()) {
                                realOfferSymbolMapper.updateById(addPojo);
                            } else {
                                addErrorInfo2.append(realOfferSymbol.getSymbolName()).append(" ");
                            }
                        } else {
                            addErrorInfo2.append(realOfferSymbol.getSymbolName()).append(" ");
                        }
                    } else {
                        realOfferSymbolMapper.updateById(addPojo);
                    }
                } else {
                    addErrorInfo.append(realOfferSymbol.getSymbolName()).append(" ");
                }
            }
            if (addErrorInfo.toString().length() > 2) {
                addErrorInfo.append("】").append(" 尚未初始化");
            }
            if (addErrorInfo2.toString().length() > 2) {
                addErrorInfo2.append("】").append(" 启动失败,请重试");
            }
        }
        if (CollectionUtils.isNotEmpty(subCurrencyList)) {
            for (Long subCurrencyId : subCurrencyList) {
                RealOfferSymbol realOfferSymbol = realOfferSymbolMapper.selectById(subCurrencyId);
                if (null != realOfferSymbol && realOfferSymbol.getSymbolStatus() != 5) {
                    if (realOffer.getStatus().equals("运行中")) {
                        RealOfferSymbol addPojo = new RealOfferSymbol();
                        addPojo.setId(subCurrencyId);
                        addPojo.setSymbolStatus(4);
                        realOfferSymbolMapper.updateById(addPojo);
                        /*// 调用删除接口（暂停交易对） 成功后进行数据库操作
                        Symbol symbol = symbolMapper.selectById(realOfferSymbol.getSymbolId());
                        Map<String, Object> reqForm = new HashMap<>();
                        List<String> exchangeList = new ArrayList<>();
                        reqForm.put("symbol", symbol.getName());
                        exchangeList = getExchangeNameList(realOfferSymbol, realOfferId);
                        reqForm.put("exchanges", exchangeList);
                        String jsonStr = JSONUtil.toJsonStr(reqForm);
                        String key = AESUtil.getKey(realOffer.getAppId());
                        String encrypt = AESUtil.encryptCBC(jsonStr, key);
                        String reqUrl = "http://"+server.getServerIp()+":"+realOffer.getRealOfferPort()+"/api/v1/removeSymbol";
                        HttpResponse response = HttpRequest
                                .post(reqUrl)
                                .body(encrypt)
                                .header(Header.CONTENT_TYPE, "application/json")
                                .header("Data-Length", String.valueOf(jsonStr.length()))
                                .timeout(60 * 1000)
                                .execute();
                        System.out.println(response.body());
                        if (response.isOk()) {
                            String result = response.body();
                            QntRobotResponse qntRobotResponse = JSONUtil.toBean(result, QntRobotResponse.class);
                            System.out.println(qntRobotResponse.getSuccess() + ":" + result);
                            if(qntRobotResponse.getSuccess()){
                                RealOfferSymbol addPojo = new RealOfferSymbol();
                                addPojo.setId(subCurrencyId);
                                addPojo.setSymbolStatus(4);
                                realOfferSymbolMapper.updateById(addPojo);
                            } else {
                                subErrorInfo2.append(realOfferSymbol.getSymbolName()).append(" ");
                            }
                        } else {
                            subErrorInfo2.append(realOfferSymbol.getSymbolName()).append(" ");
                        }*/
                    } else {
                        RealOfferSymbol addPojo = new RealOfferSymbol();
                        addPojo.setId(subCurrencyId);
                        addPojo.setSymbolStatus(4);
                        realOfferSymbolMapper.updateById(addPojo);
                    }
                } else {
                    subErrorInfo.append(realOfferSymbol.getSymbolName()).append(" ");
                }
            }
            if (subErrorInfo.toString().length() > 2) {
                subErrorInfo.append("】").append(" 尚未初始化");
            }
            if (subErrorInfo2.toString().length() > 2) {
                subErrorInfo2.append("】").append(" 暂停失败,请重试");
            }
        }
        if (addErrorInfo.toString().length() > 2) {
            errorResp.append(addErrorInfo);
        }
        if (addErrorInfo2.toString().length() > 2) {
            errorResp.append(addErrorInfo2);
        }
        if (subErrorInfo.toString().length() > 2) {
            errorResp.append(subErrorInfo);
        }
        if (subErrorInfo2.toString().length() > 2) {
            errorResp.append(subErrorInfo2);
        }
        if (errorResp.toString().length() > 0) {
            return Result.error(errorResp.toString(), null);
        }
        return Result.ok();
    }

    public Result<?> getRealOfferRevenueChartData(RealOfferRevenueChartDataReq req) {
        List<Map<String, Object>> result = new ArrayList<>();
        RealOffer realOffer = this.baseMapper.selectById(req.getId());
        final LambdaQueryWrapper<Trade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Trade::getAppId, realOffer.getAppId());
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
            dataList.add(record.getAppProfit());
            dataMap.put("value", dataList);
            return dataMap;
        }).toList();
        return Result.ok(result);
    }

    public Result<?> getRealOfferStrategyChartData(RealOfferRevenueChartDataReq req) {
        List<Map<String, Object>> result = new ArrayList<>();
        final LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Asset::getRealId, req.getId());
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


    public Result<?> getLogTableData(RealOfferLogReq req) {
        RealOffer realOffer = this.baseMapper.selectById(req.getId());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        final int page = req.getPage();
        final int size = req.getSize();
        // 查询分页列表
        final LambdaQueryWrapper<Trade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Trade::getAppId, realOffer.getAppId());
        wrapper.orderByDesc(Trade::getCreateTime);
        final Page<Trade> tradePage = tradeMapper.selectPage(new Page<>(page, size), wrapper);
        // 转为响应对象
        List<RealOfferLogResp> realOfferLogResps = tradePage.getRecords().stream().map(record -> {
            RealOfferLogResp realOfferLogResp = new RealOfferLogResp();
            realOfferLogResp.setLogType("信息");
            if (record.getProfit().contains("-")) {
                realOfferLogResp.setProfitFlag(0);
            } else {
                realOfferLogResp.setProfitFlag(1);
            }
            final LambdaQueryWrapper<TradeOrder> tradeOrderWrapper = new LambdaQueryWrapper<>();
            tradeOrderWrapper.eq(TradeOrder::getTradeId, record.getTradeId());
            List<TradeOrder> tradeOrders = tradeOrderMapper.selectList(tradeOrderWrapper);
            if (tradeOrders.size() != 2) {
                return realOfferLogResp;
            }
            TradeOrder buyOrder = new TradeOrder();
            TradeOrder sellOrder = new TradeOrder();
            if (tradeOrders.get(0).getSide().equals("buy")) {
                buyOrder = tradeOrders.get(0);
                sellOrder = tradeOrders.get(1);
            } else {
                buyOrder = tradeOrders.get(1);
                sellOrder = tradeOrders.get(0);
            }
            if (null != buyOrder.getOpenTs() && buyOrder.getOpenTs() != 0L) {
                realOfferLogResp.setLogTime(sdf.format(new Date(buyOrder.getOpenTs())));
            } else {
                realOfferLogResp.setLogTime(sdf.format(new Date(sellOrder.getOpenTs())));
            }

            StringBuilder logInfo = new StringBuilder();
            logInfo.append("【");
            logInfo.append(record.getSymbol().split("-")[0]);
            logInfo.append("】 ");
            logInfo.append(record.getTradeId());
            logInfo.append(" ");
            if (record.getMode().equals("arbitrage")) {
                logInfo.append("套利");
            } else if (record.getMode().equals("balance")) {
                logInfo.append("均衡");
            } else if (record.getMode().equals("hedge")) {
                logInfo.append("对冲");
            }
            logInfo.append(" ");
            logInfo.append("收益:");
            logInfo.append(record.getProfit());
            logInfo.append(" 【");
            logInfo.append(buyOrder.getExchange());
            logInfo.append("@");
            logInfo.append(record.getSymbol());
            logInfo.append("买入@[");
            logInfo.append(buyOrder.getOpenPrice());
            logInfo.append("]");
            logInfo.append(buyOrder.getClosePrice());
            logInfo.append("@");
            logInfo.append(buyOrder.getCloseAmount());
            logInfo.append("@");
            BigDecimal buyProfit = BigDecimal.valueOf(Double.parseDouble(buyOrder.getClosePrice())).multiply(BigDecimal.valueOf(Double.parseDouble(buyOrder.getCloseAmount())));
            logInfo.append(String.valueOf(buyProfit));
            logInfo.append(" => ");
            logInfo.append(sellOrder.getExchange());
            logInfo.append("@");
            logInfo.append(record.getSymbol());
            logInfo.append("卖出@[");
            logInfo.append(sellOrder.getOpenPrice());
            logInfo.append("]");
            logInfo.append(sellOrder.getClosePrice());
            logInfo.append("@");
            logInfo.append(sellOrder.getCloseAmount());
            logInfo.append("@");
            BigDecimal sellProfit = BigDecimal.valueOf(Double.parseDouble(sellOrder.getClosePrice())).multiply(BigDecimal.valueOf(Double.parseDouble(sellOrder.getCloseAmount())));
            logInfo.append(String.valueOf(sellProfit));
            logInfo.append("】");
            logInfo.append("耗时");
            if (!record.getMode().equals("hedge")) {
                logInfo.append(buyOrder.getOpenTs() - buyOrder.getDepthTs());
                logInfo.append("ms,");
                logInfo.append(sellOrder.getOpenTs() - sellOrder.getDepthTs());
                logInfo.append("ms,");
            }
            logInfo.append(buyOrder.getCloseTs() - buyOrder.getOpenTs());
            logInfo.append("ms,");
            logInfo.append(sellOrder.getCloseTs() - sellOrder.getOpenTs());
            logInfo.append("ms");
            realOfferLogResp.setLogInfo(logInfo.toString());
            return realOfferLogResp;
        }).toList();
        final RealOfferLogPageResp realOfferLogPageResp = new RealOfferLogPageResp();
        realOfferLogPageResp.setTotal(tradePage.getTotal());
        realOfferLogPageResp.setRecords(realOfferLogResps);
        return Result.ok(realOfferLogPageResp);
    }

    public Result<?> getLogTableData2(RealOfferLogReq req) {
        final int page = req.getPage();
        final int size = req.getSize();
        // 查询分页列表
        final LambdaQueryWrapper<RealOfferRunLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RealOfferRunLog::getRealOfferId, req.getId());
        wrapper.orderByDesc(RealOfferRunLog::getCreateTime);
        Page<RealOfferRunLog> realOfferRunLogPage = realOfferRunLogMapper.selectPage(new Page<>(page, size), wrapper);
        final RealOfferRunLogPageResp realOfferRunLogPageResp = new RealOfferRunLogPageResp();
        realOfferRunLogPageResp.setTotal(realOfferRunLogPage.getTotal());
        realOfferRunLogPageResp.setRecords(realOfferRunLogPage.getRecords());
        return Result.ok(realOfferRunLogPageResp);
    }

    public Result<?> getRealOfferExchangeList(Long id) {
        final LambdaQueryWrapper<RealOfferExchange> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RealOfferExchange::getRealOfferId, id);
        List<RealOfferExchangeResp> realOfferExchangeResps = realOfferExchangeMapper.selectList(wrapper).stream().map(record -> {
            RealOfferExchangeResp realOfferExchangeResp = new RealOfferExchangeResp();
            BeanUtils.copyProperties(record, realOfferExchangeResp);
            final LambdaQueryWrapper<Exchange> exchangeWrapper = new LambdaQueryWrapper<>();
            exchangeWrapper.eq(Exchange::getExchange, record.getExchange());
            realOfferExchangeResp.setExchangeName(exchangeMapper.selectOne(exchangeWrapper).getExchangeNameWeb());
            if (record.getStatus() == 1) {
                realOfferExchangeResp.setExchangeStatus("启用");
            } else if (record.getStatus() == 0) {
                realOfferExchangeResp.setExchangeStatus("禁止");
            }
            // 分配偏差 A交易所分配偏差 = A交易所资产 - 总资产/（各交易所交易对数之和）*A交易所交易对数
            // 例如： 火币交易所分配偏差=  8238       - 95663/(18+5+48+32+47+0+32+22)*18

            // 增量溢价
            return realOfferExchangeResp;
        }).toList();
        return Result.ok(realOfferExchangeResps);
    }

    public Result<?> getRealOfferEnableSymbolList(Long id) {
        Date now = new Date();
        long nowTime = now.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        RealOffer realOffer = this.baseMapper.selectById(id);
        final LambdaQueryWrapper<RealOfferSymbol> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RealOfferSymbol::getRealOfferId, id);
        wrapper.eq(RealOfferSymbol::getSymbolStatus, 1);
        List<RealOfferSymbolResp> realOfferSymbolResps = realOfferSymbolMapper.selectList(wrapper).stream().map(record -> {
            RealOfferSymbolResp realOfferSymbolResp = new RealOfferSymbolResp();
            BeanUtils.copyProperties(record, realOfferSymbolResp);
            // 摊薄成本 只显示买入成本
            if(null != record.getBuyCost()){
                record.setDilutedCosts(record.getBuyCost());
            }
            // 延迟
            realOfferSymbolResp.setDelayTimeConsumingPolling(record.getDelayTime() + "|" + record.getConsumingTime() + "|" + record.getPollingTime());
            // 均衡溢价
            realOfferSymbolResp.setEquilibriumPremium(realOffer.getInitialEquilibriumPremium());
            // 交易溢价
            realOfferSymbolResp.setTransactionPremium(realOffer.getArbitragePremium());
            // 挂单大小
            realOfferSymbolResp.setOrderSize(realOffer.getMaxPendingOrder());
            // 挂单距离
            realOfferSymbolResp.setOrderDistance(realOffer.getOrderDistance());
            // 交易对个数
            int canTraningExchangeNum = 0;
            Symbol symbol = symbolMapper.selectById(record.getSymbolId());
            if (symbol.getExBitmart() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExBybit() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExCoinex() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExHtx() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExOkx() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExBitget() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExKucoin() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExGate() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExMexc() == 1) {
                canTraningExchangeNum++;
            }
            final LambdaQueryWrapper<RealOfferSymbolExchange> realOfferSymbolExchangeWrapper = new LambdaQueryWrapper<>();
            realOfferSymbolExchangeWrapper.eq(RealOfferSymbolExchange::getRealOfferSymbolId, record.getId());
            realOfferSymbolExchangeWrapper.eq(RealOfferSymbolExchange::getStatus, 1);
            Long enableExchangeNum = realOfferSymbolExchangeMapper.selectCount(realOfferSymbolExchangeWrapper);
            realOfferSymbolResp.setTransactionPairs(enableExchangeNum + "|" + canTraningExchangeNum);
            // 启用时长(D)
            Date updateTime = record.getOperationTime();
            if(null != updateTime){
                long time = updateTime.getTime();
                long duration = nowTime - time;
                BigDecimal oneDayTime = new BigDecimal(1000 * 60 * 60 * 24);
                BigDecimal durationB = BigDecimal.valueOf(duration).divide(oneDayTime, 2, RoundingMode.HALF_UP);
                realOfferSymbolResp.setEnableDuration(String.valueOf(durationB));
            }
            // 持仓盈亏(U)
            BigDecimal holding = new BigDecimal(0);
            if (null != record.getHoldingValue()) {
                holding = BigDecimal.valueOf(Double.parseDouble(record.getHoldingValue()));
                if(null != record.getBuyCost()){
                    BigDecimal buy = BigDecimal.valueOf(Double.parseDouble(record.getBuyCost()));
                    realOfferSymbolResp.setHoldingPhase(String.valueOf(holding.subtract(buy).setScale(2, RoundingMode.HALF_UP)));
                }
            }
            // 套利盈亏(U)
            final LambdaQueryWrapper<Trade> tradeWrapper = new LambdaQueryWrapper<>();
            tradeWrapper.eq(Trade::getAppId, realOffer.getAppId());
            tradeWrapper.like(Trade::getSymbol, "%"+record.getSymbolName()+"%");
            tradeWrapper.orderByDesc(Trade::getId);
            tradeWrapper.last("limit 1");
            List<Trade> currentTradeList = tradeMapper.selectList(tradeWrapper);
            Trade currentTrade = new Trade();
            BigDecimal currentSymbolProfit = new BigDecimal(0);
            if(CollectionUtils.isNotEmpty(currentTradeList)){
                currentTrade = currentTradeList.get(0);
                if(null != currentTrade && null != currentTrade.getSymbolProfit()){
                    currentSymbolProfit = BigDecimal.valueOf(Double.parseDouble(currentTrade.getSymbolProfit()));
                    realOfferSymbolResp.setArbitragePhase(String.valueOf(currentSymbolProfit.setScale(2, RoundingMode.HALF_UP)));
                }
            }
            // 1h盈亏(U)
            long oneHourDur = nowTime-1000*60*60;
            Date oneHourDurD = new Date(oneHourDur);
            tradeWrapper.clear();
            tradeWrapper.eq(Trade::getAppId, realOffer.getAppId());
            tradeWrapper.ge(Trade::getCreateTime, format.format(oneHourDurD));
            tradeWrapper.like(Trade::getSymbol, "%"+record.getSymbolName()+"%");
            tradeWrapper.orderByAsc(Trade::getId);
            tradeWrapper.last("limit 1");
            List<Trade> oneHourAgoTradeList = tradeMapper.selectList(tradeWrapper);
            BigDecimal oneHourAgoSymbolProfit = new BigDecimal(0);
            Trade oneHourAgoTrade = new Trade();
            if(CollectionUtils.isNotEmpty(oneHourAgoTradeList)){
                oneHourAgoTrade = oneHourAgoTradeList.get(0);
                if(null != oneHourAgoTrade && null != oneHourAgoTrade.getSymbolProfit()){
                    oneHourAgoSymbolProfit = BigDecimal.valueOf(Double.parseDouble(oneHourAgoTrade.getSymbolProfit()));
                    realOfferSymbolResp.setOneHourPhase(String.valueOf(currentSymbolProfit.subtract(oneHourAgoSymbolProfit).setScale(2, RoundingMode.HALF_UP)));
                }
            }
            // 4h盈亏(U)
            long fourHourDur = nowTime-1000*60*60*4;
            Date fourHourDurD = new Date(fourHourDur);
            tradeWrapper.clear();
            tradeWrapper.eq(Trade::getAppId, realOffer.getAppId());
            tradeWrapper.ge(Trade::getCreateTime, format.format(fourHourDurD));
            tradeWrapper.like(Trade::getSymbol, "%"+record.getSymbolName()+"%");
            tradeWrapper.orderByAsc(Trade::getId);
            tradeWrapper.last("limit 1");
            List<Trade> fourHourAgoTradeList = tradeMapper.selectList(tradeWrapper);
            BigDecimal fourHourAgoSymbolProfit = new BigDecimal(0);
            Trade fourHourAgoTrade = new Trade();
            if(CollectionUtils.isNotEmpty(fourHourAgoTradeList)){
                fourHourAgoTrade = fourHourAgoTradeList.get(0);
                if(null != fourHourAgoTrade && null != fourHourAgoTrade.getSymbolProfit()){
                    fourHourAgoSymbolProfit = BigDecimal.valueOf(Double.parseDouble(fourHourAgoTrade.getSymbolProfit()));
                    realOfferSymbolResp.setFourHourPhase(String.valueOf(currentSymbolProfit.subtract(fourHourAgoSymbolProfit).setScale(2, RoundingMode.HALF_UP)));
                }
            }
            // 24h盈亏(U)
            long oneDayDur = nowTime-1000*60*60*24;
            Date oneDayDurD = new Date(oneDayDur);
            tradeWrapper.clear();
            tradeWrapper.eq(Trade::getAppId, realOffer.getAppId());
            tradeWrapper.ge(Trade::getCreateTime, format.format(oneDayDurD));
            tradeWrapper.like(Trade::getSymbol, "%"+record.getSymbolName()+"%");
            tradeWrapper.orderByAsc(Trade::getId);
            tradeWrapper.last("limit 1");
            List<Trade> oneDayAgoTradeList = tradeMapper.selectList(tradeWrapper);
            BigDecimal oneDayAgoSymbolProfit = new BigDecimal(0);
            Trade oneDayAgoTrade = new Trade();
            if(CollectionUtils.isNotEmpty(oneDayAgoTradeList)){
                oneDayAgoTrade = oneDayAgoTradeList.get(0);
                if(null != oneDayAgoTrade && null != oneDayAgoTrade.getSymbolProfit()){
                    oneDayAgoSymbolProfit = BigDecimal.valueOf(Double.parseDouble(oneDayAgoTrade.getSymbolProfit()));
                    realOfferSymbolResp.setOneDayPhase(String.valueOf(currentSymbolProfit.subtract(oneDayAgoSymbolProfit).setScale(2, RoundingMode.HALF_UP)));
                }
            }
            // 3D盈亏(U)
            long threeDayDur = nowTime-1000*60*60*24*3;
            Date threeDayDurD = new Date(threeDayDur);
            tradeWrapper.clear();
            tradeWrapper.eq(Trade::getAppId, realOffer.getAppId());
            tradeWrapper.ge(Trade::getCreateTime, format.format(threeDayDurD));
            tradeWrapper.like(Trade::getSymbol, "%"+record.getSymbolName()+"%");
            tradeWrapper.orderByAsc(Trade::getId);
            tradeWrapper.last("limit 1");
            List<Trade> threeDayAgoTradeList = tradeMapper.selectList(tradeWrapper);
            BigDecimal threeDayAgoSymbolProfit = new BigDecimal(0);
            BigDecimal threeDayDurSymbolProfit = new BigDecimal(0);
            Trade threeDayAgoTrade = new Trade();
            if(CollectionUtils.isNotEmpty(threeDayAgoTradeList)){
                threeDayAgoTrade = threeDayAgoTradeList.get(0);
                if(null != threeDayAgoTrade && null != threeDayAgoTrade.getSymbolProfit()){
                    threeDayAgoSymbolProfit = BigDecimal.valueOf(Double.parseDouble(threeDayAgoTrade.getSymbolProfit()));
                    threeDayDurSymbolProfit = currentSymbolProfit.subtract(threeDayAgoSymbolProfit);
                    realOfferSymbolResp.setThreeDayPhase(String.valueOf(threeDayDurSymbolProfit.setScale(2, RoundingMode.HALF_UP)));
                }
            }
            // 3D月化(U)
            if(holding.compareTo(BigDecimal.ZERO) > 0){
                BigDecimal threeDayMouthly = new BigDecimal(0);
                threeDayMouthly = threeDayDurSymbolProfit.multiply(BigDecimal.valueOf(10)).divide(holding,4,RoundingMode.HALF_UP);
                realOfferSymbolResp.setThreeDayMonthly(String.valueOf(threeDayMouthly.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)));
            }
            return realOfferSymbolResp;
        }).toList();
        return Result.ok(realOfferSymbolResps);
    }

    public Result<?> getRealOfferClearingSymbolList(Long id) {
        Date now = new Date();
        long nowTime = now.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        RealOffer realOffer = this.baseMapper.selectById(id);
        final LambdaQueryWrapper<RealOfferSymbol> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RealOfferSymbol::getRealOfferId, id);
        wrapper.eq(RealOfferSymbol::getSymbolStatus, 2);
        List<RealOfferSymbolResp> realOfferSymbolResps = realOfferSymbolMapper.selectList(wrapper).stream().map(record -> {
            RealOfferSymbolResp realOfferSymbolResp = new RealOfferSymbolResp();
            BeanUtils.copyProperties(record, realOfferSymbolResp);
            realOfferSymbolResp.setDelayTimeConsumingPolling(record.getDelayTime() + "|" + record.getConsumingTime() + "|" + record.getPollingTime());
            realOfferSymbolResp.setEquilibriumPremium(realOffer.getInitialEquilibriumPremium());
            realOfferSymbolResp.setTransactionPremium(realOffer.getArbitragePremium());
            realOfferSymbolResp.setOrderSize(realOffer.getMaxPendingOrder());
            realOfferSymbolResp.setOrderDistance(realOffer.getOrderDistance());
            // 摊薄成本 只显示买入成本
            if(null != record.getBuyCost()){
                record.setDilutedCosts(record.getBuyCost());
            }
            // 延迟
            realOfferSymbolResp.setDelayTimeConsumingPolling(record.getDelayTime() + "|" + record.getConsumingTime() + "|" + record.getPollingTime());
            // 均衡溢价
            realOfferSymbolResp.setEquilibriumPremium(realOffer.getInitialEquilibriumPremium());
            // 交易溢价
            realOfferSymbolResp.setTransactionPremium(realOffer.getArbitragePremium());
            // 挂单大小
            realOfferSymbolResp.setOrderSize(realOffer.getMaxPendingOrder());
            // 挂单距离
            realOfferSymbolResp.setOrderDistance(realOffer.getOrderDistance());
            // 交易对个数
            int canTraningExchangeNum = 0;
            Symbol symbol = symbolMapper.selectById(record.getSymbolId());
            if (symbol.getExBitmart() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExBybit() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExCoinex() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExHtx() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExOkx() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExBitget() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExKucoin() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExGate() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExMexc() == 1) {
                canTraningExchangeNum++;
            }
            final LambdaQueryWrapper<RealOfferSymbolExchange> realOfferSymbolExchangeWrapper = new LambdaQueryWrapper<>();
            realOfferSymbolExchangeWrapper.eq(RealOfferSymbolExchange::getRealOfferSymbolId, record.getId());
            realOfferSymbolExchangeWrapper.eq(RealOfferSymbolExchange::getStatus, 1);
            Long enableExchangeNum = realOfferSymbolExchangeMapper.selectCount(realOfferSymbolExchangeWrapper);
            realOfferSymbolResp.setTransactionPairs(enableExchangeNum + "|" + canTraningExchangeNum);
            // 启用时长(D)
            Date updateTime = record.getOperationTime();
            if(null != updateTime){
                long time = updateTime.getTime();
                long duration = nowTime - time;
                BigDecimal oneDayTime = new BigDecimal(1000 * 60 * 60 * 24);
                BigDecimal durationB = BigDecimal.valueOf(duration).divide(oneDayTime, 2, RoundingMode.HALF_UP);
                realOfferSymbolResp.setEnableDuration(String.valueOf(durationB));
            }
            // 持仓盈亏(U)
            BigDecimal holding = new BigDecimal(0);
            if (null != record.getHoldingValue()) {
                holding = BigDecimal.valueOf(Double.parseDouble(record.getHoldingValue()));
                if(null != record.getBuyCost()){
                    BigDecimal buy = BigDecimal.valueOf(Double.parseDouble(record.getBuyCost()));
                    realOfferSymbolResp.setHoldingPhase(String.valueOf(holding.subtract(buy).setScale(2, RoundingMode.HALF_UP)));
                }
            }
            // 套利盈亏(U)
            final LambdaQueryWrapper<Trade> tradeWrapper = new LambdaQueryWrapper<>();
            tradeWrapper.eq(Trade::getAppId, realOffer.getAppId());
            tradeWrapper.like(Trade::getSymbol, "%"+record.getSymbolName()+"%");
            tradeWrapper.orderByDesc(Trade::getId);
            tradeWrapper.last("limit 1");
            List<Trade> currentTradeList = tradeMapper.selectList(tradeWrapper);
            Trade currentTrade = new Trade();
            BigDecimal currentSymbolProfit = new BigDecimal(0);
            if(CollectionUtils.isNotEmpty(currentTradeList)){
                currentTrade = currentTradeList.get(0);
                if(null != currentTrade && null != currentTrade.getSymbolProfit()){
                    currentSymbolProfit = BigDecimal.valueOf(Double.parseDouble(currentTrade.getSymbolProfit()));
                    realOfferSymbolResp.setArbitragePhase(String.valueOf(currentSymbolProfit.setScale(2, RoundingMode.HALF_UP)));
                }
            }
            // 1h盈亏(U)
            long oneHourDur = nowTime-1000*60*60;
            Date oneHourDurD = new Date(oneHourDur);
            tradeWrapper.clear();
            tradeWrapper.eq(Trade::getAppId, realOffer.getAppId());
            tradeWrapper.ge(Trade::getCreateTime, format.format(oneHourDurD));
            tradeWrapper.like(Trade::getSymbol, "%"+record.getSymbolName()+"%");
            tradeWrapper.orderByAsc(Trade::getId);
            tradeWrapper.last("limit 1");
            List<Trade> oneHourAgoTradeList = tradeMapper.selectList(tradeWrapper);
            BigDecimal oneHourAgoSymbolProfit = new BigDecimal(0);
            Trade oneHourAgoTrade = new Trade();
            if(CollectionUtils.isNotEmpty(oneHourAgoTradeList)){
                oneHourAgoTrade = oneHourAgoTradeList.get(0);
                if(null != oneHourAgoTrade && null != oneHourAgoTrade.getSymbolProfit()){
                    oneHourAgoSymbolProfit = BigDecimal.valueOf(Double.parseDouble(oneHourAgoTrade.getSymbolProfit()));
                    realOfferSymbolResp.setOneHourPhase(String.valueOf(currentSymbolProfit.subtract(oneHourAgoSymbolProfit).setScale(2, RoundingMode.HALF_UP)));
                }
            }
            // 4h盈亏(U)
            long fourHourDur = nowTime-1000*60*60*4;
            Date fourHourDurD = new Date(fourHourDur);
            tradeWrapper.clear();
            tradeWrapper.eq(Trade::getAppId, realOffer.getAppId());
            tradeWrapper.ge(Trade::getCreateTime, format.format(fourHourDurD));
            tradeWrapper.like(Trade::getSymbol, "%"+record.getSymbolName()+"%");
            tradeWrapper.orderByAsc(Trade::getId);
            tradeWrapper.last("limit 1");
            List<Trade> fourHourAgoTradeList = tradeMapper.selectList(tradeWrapper);
            BigDecimal fourHourAgoSymbolProfit = new BigDecimal(0);
            Trade fourHourAgoTrade = new Trade();
            if(CollectionUtils.isNotEmpty(fourHourAgoTradeList)){
                fourHourAgoTrade = fourHourAgoTradeList.get(0);
                if(null != fourHourAgoTrade && null != fourHourAgoTrade.getSymbolProfit()){
                    fourHourAgoSymbolProfit = BigDecimal.valueOf(Double.parseDouble(fourHourAgoTrade.getSymbolProfit()));
                    realOfferSymbolResp.setFourHourPhase(String.valueOf(currentSymbolProfit.subtract(fourHourAgoSymbolProfit).setScale(2, RoundingMode.HALF_UP)));
                }
            }
            // 24h盈亏(U)
            long oneDayDur = nowTime-1000*60*60*24;
            Date oneDayDurD = new Date(oneDayDur);
            tradeWrapper.clear();
            tradeWrapper.eq(Trade::getAppId, realOffer.getAppId());
            tradeWrapper.ge(Trade::getCreateTime, format.format(oneDayDurD));
            tradeWrapper.like(Trade::getSymbol, "%"+record.getSymbolName()+"%");
            tradeWrapper.orderByAsc(Trade::getId);
            tradeWrapper.last("limit 1");
            List<Trade> oneDayAgoTradeList = tradeMapper.selectList(tradeWrapper);
            BigDecimal oneDayAgoSymbolProfit = new BigDecimal(0);
            Trade oneDayAgoTrade = new Trade();
            if(CollectionUtils.isNotEmpty(oneDayAgoTradeList)){
                oneDayAgoTrade = oneDayAgoTradeList.get(0);
                if(null != oneDayAgoTrade && null != oneDayAgoTrade.getSymbolProfit()){
                    oneDayAgoSymbolProfit = BigDecimal.valueOf(Double.parseDouble(oneDayAgoTrade.getSymbolProfit()));
                    realOfferSymbolResp.setOneDayPhase(String.valueOf(currentSymbolProfit.subtract(oneDayAgoSymbolProfit).setScale(2, RoundingMode.HALF_UP)));
                }
            }
            // 3D盈亏(U)
            long threeDayDur = nowTime-1000*60*60*24*3;
            Date threeDayDurD = new Date(threeDayDur);
            tradeWrapper.clear();
            tradeWrapper.eq(Trade::getAppId, realOffer.getAppId());
            tradeWrapper.ge(Trade::getCreateTime, format.format(threeDayDurD));
            tradeWrapper.like(Trade::getSymbol, "%"+record.getSymbolName()+"%");
            tradeWrapper.orderByAsc(Trade::getId);
            tradeWrapper.last("limit 1");
            List<Trade> threeDayAgoTradeList = tradeMapper.selectList(tradeWrapper);
            BigDecimal threeDayAgoSymbolProfit = new BigDecimal(0);
            BigDecimal threeDayDurSymbolProfit = new BigDecimal(0);
            Trade threeDayAgoTrade = new Trade();
            if(CollectionUtils.isNotEmpty(threeDayAgoTradeList)){
                threeDayAgoTrade = threeDayAgoTradeList.get(0);
                if(null != threeDayAgoTrade && null != threeDayAgoTrade.getSymbolProfit()){
                    threeDayAgoSymbolProfit = BigDecimal.valueOf(Double.parseDouble(threeDayAgoTrade.getSymbolProfit()));
                    threeDayDurSymbolProfit = currentSymbolProfit.subtract(threeDayAgoSymbolProfit);
                    realOfferSymbolResp.setThreeDayPhase(String.valueOf(threeDayDurSymbolProfit.setScale(2, RoundingMode.HALF_UP)));
                }
            }
            // 3D月化(U)
            if(holding.compareTo(BigDecimal.ZERO) > 0){
                BigDecimal threeDayMouthly = new BigDecimal(0);
                threeDayMouthly = threeDayDurSymbolProfit.multiply(BigDecimal.valueOf(10)).divide(holding,4,RoundingMode.HALF_UP);
                realOfferSymbolResp.setThreeDayMonthly(String.valueOf(threeDayMouthly.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)));
            }
            return realOfferSymbolResp;
        }).toList();
        return Result.ok(realOfferSymbolResps);
    }

    public Result<?> getRealOfferLockingSymbolList(Long id) {
        Date now = new Date();
        long nowTime = now.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        RealOffer realOffer = this.baseMapper.selectById(id);
        final LambdaQueryWrapper<RealOfferSymbol> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RealOfferSymbol::getRealOfferId, id);
        wrapper.eq(RealOfferSymbol::getSymbolStatus, 3);
        List<RealOfferSymbolResp> realOfferSymbolResps = realOfferSymbolMapper.selectList(wrapper).stream().map(record -> {
            RealOfferSymbolResp realOfferSymbolResp = new RealOfferSymbolResp();
            BeanUtils.copyProperties(record, realOfferSymbolResp);
            realOfferSymbolResp.setDelayTimeConsumingPolling(record.getDelayTime() + "|" + record.getConsumingTime() + "|" + record.getPollingTime());
            realOfferSymbolResp.setEquilibriumPremium(realOffer.getInitialEquilibriumPremium());
            realOfferSymbolResp.setTransactionPremium(realOffer.getArbitragePremium());
            realOfferSymbolResp.setOrderSize(realOffer.getMaxPendingOrder());
            realOfferSymbolResp.setOrderDistance(realOffer.getOrderDistance());
            // 摊薄成本 只显示买入成本
            if(null != record.getBuyCost()){
                record.setDilutedCosts(record.getBuyCost());
            }
            // 延迟
            realOfferSymbolResp.setDelayTimeConsumingPolling(record.getDelayTime() + "|" + record.getConsumingTime() + "|" + record.getPollingTime());
            // 均衡溢价
            realOfferSymbolResp.setEquilibriumPremium(realOffer.getInitialEquilibriumPremium());
            // 交易溢价
            realOfferSymbolResp.setTransactionPremium(realOffer.getArbitragePremium());
            // 挂单大小
            realOfferSymbolResp.setOrderSize(realOffer.getMaxPendingOrder());
            // 挂单距离
            realOfferSymbolResp.setOrderDistance(realOffer.getOrderDistance());
            // 交易对个数
            int canTraningExchangeNum = 0;
            Symbol symbol = symbolMapper.selectById(record.getSymbolId());
            if (symbol.getExBitmart() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExBybit() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExCoinex() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExHtx() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExOkx() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExBitget() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExKucoin() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExGate() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExMexc() == 1) {
                canTraningExchangeNum++;
            }
            final LambdaQueryWrapper<RealOfferSymbolExchange> realOfferSymbolExchangeWrapper = new LambdaQueryWrapper<>();
            realOfferSymbolExchangeWrapper.eq(RealOfferSymbolExchange::getRealOfferSymbolId, record.getId());
            realOfferSymbolExchangeWrapper.eq(RealOfferSymbolExchange::getStatus, 1);
            Long enableExchangeNum = realOfferSymbolExchangeMapper.selectCount(realOfferSymbolExchangeWrapper);
            realOfferSymbolResp.setTransactionPairs(enableExchangeNum + "|" + canTraningExchangeNum);
            // 启用时长(D)
            Date updateTime = record.getOperationTime();
            if(null != updateTime){
                long time = updateTime.getTime();
                long duration = nowTime - time;
                BigDecimal oneDayTime = new BigDecimal(1000 * 60 * 60 * 24);
                BigDecimal durationB = BigDecimal.valueOf(duration).divide(oneDayTime, 2, RoundingMode.HALF_UP);
                realOfferSymbolResp.setEnableDuration(String.valueOf(durationB));
            }
            // 持仓盈亏(U)
            BigDecimal holding = new BigDecimal(0);
            if (null != record.getHoldingValue()) {
                holding = BigDecimal.valueOf(Double.parseDouble(record.getHoldingValue()));
                if(null != record.getBuyCost()){
                    BigDecimal buy = BigDecimal.valueOf(Double.parseDouble(record.getBuyCost()));
                    realOfferSymbolResp.setHoldingPhase(String.valueOf(holding.subtract(buy).setScale(2, RoundingMode.HALF_UP)));
                }
            }
            // 套利盈亏(U)
            final LambdaQueryWrapper<Trade> tradeWrapper = new LambdaQueryWrapper<>();
            tradeWrapper.eq(Trade::getAppId, realOffer.getAppId());
            tradeWrapper.like(Trade::getSymbol, "%"+record.getSymbolName()+"%");
            tradeWrapper.orderByDesc(Trade::getId);
            tradeWrapper.last("limit 1");
            List<Trade> currentTradeList = tradeMapper.selectList(tradeWrapper);
            Trade currentTrade = new Trade();
            BigDecimal currentSymbolProfit = new BigDecimal(0);
            if(CollectionUtils.isNotEmpty(currentTradeList)){
                currentTrade = currentTradeList.get(0);
                if(null != currentTrade && null != currentTrade.getSymbolProfit()){
                    currentSymbolProfit = BigDecimal.valueOf(Double.parseDouble(currentTrade.getSymbolProfit()));
                    realOfferSymbolResp.setArbitragePhase(String.valueOf(currentSymbolProfit.setScale(2, RoundingMode.HALF_UP)));
                }
            }
            // 1h盈亏(U)
            long oneHourDur = nowTime-1000*60*60;
            Date oneHourDurD = new Date(oneHourDur);
            tradeWrapper.clear();
            tradeWrapper.eq(Trade::getAppId, realOffer.getAppId());
            tradeWrapper.ge(Trade::getCreateTime, format.format(oneHourDurD));
            tradeWrapper.like(Trade::getSymbol, "%"+record.getSymbolName()+"%");
            tradeWrapper.orderByAsc(Trade::getId);
            tradeWrapper.last("limit 1");
            List<Trade> oneHourAgoTradeList = tradeMapper.selectList(tradeWrapper);
            BigDecimal oneHourAgoSymbolProfit = new BigDecimal(0);
            Trade oneHourAgoTrade = new Trade();
            if(CollectionUtils.isNotEmpty(oneHourAgoTradeList)){
                oneHourAgoTrade = oneHourAgoTradeList.get(0);
                if(null != oneHourAgoTrade && null != oneHourAgoTrade.getSymbolProfit()){
                    oneHourAgoSymbolProfit = BigDecimal.valueOf(Double.parseDouble(oneHourAgoTrade.getSymbolProfit()));
                    realOfferSymbolResp.setOneHourPhase(String.valueOf(currentSymbolProfit.subtract(oneHourAgoSymbolProfit).setScale(2, RoundingMode.HALF_UP)));
                }
            }
            // 4h盈亏(U)
            long fourHourDur = nowTime-1000*60*60*4;
            Date fourHourDurD = new Date(fourHourDur);
            tradeWrapper.clear();
            tradeWrapper.eq(Trade::getAppId, realOffer.getAppId());
            tradeWrapper.ge(Trade::getCreateTime, format.format(fourHourDurD));
            tradeWrapper.like(Trade::getSymbol, "%"+record.getSymbolName()+"%");
            tradeWrapper.orderByAsc(Trade::getId);
            tradeWrapper.last("limit 1");
            List<Trade> fourHourAgoTradeList = tradeMapper.selectList(tradeWrapper);
            BigDecimal fourHourAgoSymbolProfit = new BigDecimal(0);
            Trade fourHourAgoTrade = new Trade();
            if(CollectionUtils.isNotEmpty(fourHourAgoTradeList)){
                fourHourAgoTrade = fourHourAgoTradeList.get(0);
                if(null != fourHourAgoTrade && null != fourHourAgoTrade.getSymbolProfit()){
                    fourHourAgoSymbolProfit = BigDecimal.valueOf(Double.parseDouble(fourHourAgoTrade.getSymbolProfit()));
                    realOfferSymbolResp.setFourHourPhase(String.valueOf(currentSymbolProfit.subtract(fourHourAgoSymbolProfit).setScale(2, RoundingMode.HALF_UP)));
                }
            }
            // 24h盈亏(U)
            long oneDayDur = nowTime-1000*60*60*24;
            Date oneDayDurD = new Date(oneDayDur);
            tradeWrapper.clear();
            tradeWrapper.eq(Trade::getAppId, realOffer.getAppId());
            tradeWrapper.ge(Trade::getCreateTime, format.format(oneDayDurD));
            tradeWrapper.like(Trade::getSymbol, "%"+record.getSymbolName()+"%");
            tradeWrapper.orderByAsc(Trade::getId);
            tradeWrapper.last("limit 1");
            List<Trade> oneDayAgoTradeList = tradeMapper.selectList(tradeWrapper);
            BigDecimal oneDayAgoSymbolProfit = new BigDecimal(0);
            Trade oneDayAgoTrade = new Trade();
            if(CollectionUtils.isNotEmpty(oneDayAgoTradeList)){
                oneDayAgoTrade = oneDayAgoTradeList.get(0);
                if(null != oneDayAgoTrade && null != oneDayAgoTrade.getSymbolProfit()){
                    oneDayAgoSymbolProfit = BigDecimal.valueOf(Double.parseDouble(oneDayAgoTrade.getSymbolProfit()));
                    realOfferSymbolResp.setOneDayPhase(String.valueOf(currentSymbolProfit.subtract(oneDayAgoSymbolProfit).setScale(2, RoundingMode.HALF_UP)));
                }
            }
            // 3D盈亏(U)
            long threeDayDur = nowTime-1000*60*60*24*3;
            Date threeDayDurD = new Date(threeDayDur);
            tradeWrapper.clear();
            tradeWrapper.eq(Trade::getAppId, realOffer.getAppId());
            tradeWrapper.ge(Trade::getCreateTime, format.format(threeDayDurD));
            tradeWrapper.like(Trade::getSymbol, "%"+record.getSymbolName()+"%");
            tradeWrapper.orderByAsc(Trade::getId);
            tradeWrapper.last("limit 1");
            List<Trade> threeDayAgoTradeList = tradeMapper.selectList(tradeWrapper);
            BigDecimal threeDayAgoSymbolProfit = new BigDecimal(0);
            BigDecimal threeDayDurSymbolProfit = new BigDecimal(0);
            Trade threeDayAgoTrade = new Trade();
            if(CollectionUtils.isNotEmpty(threeDayAgoTradeList)){
                threeDayAgoTrade = threeDayAgoTradeList.get(0);
                if(null != threeDayAgoTrade && null != threeDayAgoTrade.getSymbolProfit()){
                    threeDayAgoSymbolProfit = BigDecimal.valueOf(Double.parseDouble(threeDayAgoTrade.getSymbolProfit()));
                    threeDayDurSymbolProfit = currentSymbolProfit.subtract(threeDayAgoSymbolProfit);
                    realOfferSymbolResp.setThreeDayPhase(String.valueOf(threeDayDurSymbolProfit.setScale(2, RoundingMode.HALF_UP)));
                }
            }
            // 3D月化(U)
            if(holding.compareTo(BigDecimal.ZERO) > 0){
                BigDecimal threeDayMouthly = new BigDecimal(0);
                threeDayMouthly = threeDayDurSymbolProfit.multiply(BigDecimal.valueOf(10)).divide(holding,4,RoundingMode.HALF_UP);
                realOfferSymbolResp.setThreeDayMonthly(String.valueOf(threeDayMouthly.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)));
            }
            return realOfferSymbolResp;
        }).toList();
        return Result.ok(realOfferSymbolResps);
    }

    public Result<?> getRealOfferSuspendSymbolList(Long id) {
        Date now = new Date();
        long nowTime = now.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        RealOffer realOffer = this.baseMapper.selectById(id);
        final LambdaQueryWrapper<RealOfferSymbol> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RealOfferSymbol::getRealOfferId, id);
        wrapper.eq(RealOfferSymbol::getSymbolStatus, 4);
        List<RealOfferSymbolResp> realOfferSymbolResps = realOfferSymbolMapper.selectList(wrapper).stream().map(record -> {
            RealOfferSymbolResp realOfferSymbolResp = new RealOfferSymbolResp();
            BeanUtils.copyProperties(record, realOfferSymbolResp);
            realOfferSymbolResp.setDelayTimeConsumingPolling(record.getDelayTime() + "|" + record.getConsumingTime() + "|" + record.getPollingTime());
            realOfferSymbolResp.setEquilibriumPremium(realOffer.getInitialEquilibriumPremium());
            realOfferSymbolResp.setTransactionPremium(realOffer.getArbitragePremium());
            realOfferSymbolResp.setOrderSize(realOffer.getMaxPendingOrder());
            realOfferSymbolResp.setOrderDistance(realOffer.getOrderDistance());
            // 摊薄成本 只显示买入成本
            if(null != record.getBuyCost()){
                record.setDilutedCosts(record.getBuyCost());
            }
            // 延迟
            realOfferSymbolResp.setDelayTimeConsumingPolling(record.getDelayTime() + "|" + record.getConsumingTime() + "|" + record.getPollingTime());
            // 均衡溢价
            realOfferSymbolResp.setEquilibriumPremium(realOffer.getInitialEquilibriumPremium());
            // 交易溢价
            realOfferSymbolResp.setTransactionPremium(realOffer.getArbitragePremium());
            // 挂单大小
            realOfferSymbolResp.setOrderSize(realOffer.getMaxPendingOrder());
            // 挂单距离
            realOfferSymbolResp.setOrderDistance(realOffer.getOrderDistance());
            // 交易对个数
            int canTraningExchangeNum = 0;
            Symbol symbol = symbolMapper.selectById(record.getSymbolId());
            if (symbol.getExBitmart() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExBybit() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExCoinex() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExHtx() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExOkx() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExBitget() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExKucoin() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExGate() == 1) {
                canTraningExchangeNum++;
            }
            if (symbol.getExMexc() == 1) {
                canTraningExchangeNum++;
            }
            final LambdaQueryWrapper<RealOfferSymbolExchange> realOfferSymbolExchangeWrapper = new LambdaQueryWrapper<>();
            realOfferSymbolExchangeWrapper.eq(RealOfferSymbolExchange::getRealOfferSymbolId, record.getId());
            realOfferSymbolExchangeWrapper.eq(RealOfferSymbolExchange::getStatus, 1);
            Long enableExchangeNum = realOfferSymbolExchangeMapper.selectCount(realOfferSymbolExchangeWrapper);
            realOfferSymbolResp.setTransactionPairs(enableExchangeNum + "|" + canTraningExchangeNum);
            // 启用时长(D)
            Date updateTime = record.getOperationTime();
            if(null != updateTime){
                long time = updateTime.getTime();
                long duration = nowTime - time;
                BigDecimal oneDayTime = new BigDecimal(1000 * 60 * 60 * 24);
                BigDecimal durationB = BigDecimal.valueOf(duration).divide(oneDayTime, 2, RoundingMode.HALF_UP);
                realOfferSymbolResp.setEnableDuration(String.valueOf(durationB));
            }
            // 持仓盈亏(U)
            BigDecimal holding = new BigDecimal(0);
            if (null != record.getHoldingValue()) {
                holding = BigDecimal.valueOf(Double.parseDouble(record.getHoldingValue()));
                if(null != record.getBuyCost()){
                    BigDecimal buy = BigDecimal.valueOf(Double.parseDouble(record.getBuyCost()));
                    realOfferSymbolResp.setHoldingPhase(String.valueOf(holding.subtract(buy).setScale(2, RoundingMode.HALF_UP)));
                }
            }
            // 套利盈亏(U)
            final LambdaQueryWrapper<Trade> tradeWrapper = new LambdaQueryWrapper<>();
            tradeWrapper.eq(Trade::getAppId, realOffer.getAppId());
            tradeWrapper.like(Trade::getSymbol, "%"+record.getSymbolName()+"%");
            tradeWrapper.orderByDesc(Trade::getId);
            tradeWrapper.last("limit 1");
            List<Trade> currentTradeList = tradeMapper.selectList(tradeWrapper);
            Trade currentTrade = new Trade();
            BigDecimal currentSymbolProfit = new BigDecimal(0);
            if(CollectionUtils.isNotEmpty(currentTradeList)){
                currentTrade = currentTradeList.get(0);
                if(null != currentTrade && null != currentTrade.getSymbolProfit()){
                    currentSymbolProfit = BigDecimal.valueOf(Double.parseDouble(currentTrade.getSymbolProfit()));
                    realOfferSymbolResp.setArbitragePhase(String.valueOf(currentSymbolProfit.setScale(2, RoundingMode.HALF_UP)));
                }
            }
            // 1h盈亏(U)
            long oneHourDur = nowTime-1000*60*60;
            Date oneHourDurD = new Date(oneHourDur);
            tradeWrapper.clear();
            tradeWrapper.eq(Trade::getAppId, realOffer.getAppId());
            tradeWrapper.ge(Trade::getCreateTime, format.format(oneHourDurD));
            tradeWrapper.like(Trade::getSymbol, "%"+record.getSymbolName()+"%");
            tradeWrapper.orderByAsc(Trade::getId);
            tradeWrapper.last("limit 1");
            List<Trade> oneHourAgoTradeList = tradeMapper.selectList(tradeWrapper);
            BigDecimal oneHourAgoSymbolProfit = new BigDecimal(0);
            Trade oneHourAgoTrade = new Trade();
            if(CollectionUtils.isNotEmpty(oneHourAgoTradeList)){
                oneHourAgoTrade = oneHourAgoTradeList.get(0);
                if(null != oneHourAgoTrade && null != oneHourAgoTrade.getSymbolProfit()){
                    oneHourAgoSymbolProfit = BigDecimal.valueOf(Double.parseDouble(oneHourAgoTrade.getSymbolProfit()));
                    realOfferSymbolResp.setOneHourPhase(String.valueOf(currentSymbolProfit.subtract(oneHourAgoSymbolProfit).setScale(2, RoundingMode.HALF_UP)));
                }
            }
            // 4h盈亏(U)
            long fourHourDur = nowTime-1000*60*60*4;
            Date fourHourDurD = new Date(fourHourDur);
            tradeWrapper.clear();
            tradeWrapper.eq(Trade::getAppId, realOffer.getAppId());
            tradeWrapper.ge(Trade::getCreateTime, format.format(fourHourDurD));
            tradeWrapper.like(Trade::getSymbol, "%"+record.getSymbolName()+"%");
            tradeWrapper.orderByAsc(Trade::getId);
            tradeWrapper.last("limit 1");
            List<Trade> fourHourAgoTradeList = tradeMapper.selectList(tradeWrapper);
            BigDecimal fourHourAgoSymbolProfit = new BigDecimal(0);
            Trade fourHourAgoTrade = new Trade();
            if(CollectionUtils.isNotEmpty(fourHourAgoTradeList)){
                fourHourAgoTrade = fourHourAgoTradeList.get(0);
                if(null != fourHourAgoTrade && null != fourHourAgoTrade.getSymbolProfit()){
                    fourHourAgoSymbolProfit = BigDecimal.valueOf(Double.parseDouble(fourHourAgoTrade.getSymbolProfit()));
                    realOfferSymbolResp.setFourHourPhase(String.valueOf(currentSymbolProfit.subtract(fourHourAgoSymbolProfit).setScale(2, RoundingMode.HALF_UP)));
                }
            }
            // 24h盈亏(U)
            long oneDayDur = nowTime-1000*60*60*24;
            Date oneDayDurD = new Date(oneDayDur);
            tradeWrapper.clear();
            tradeWrapper.eq(Trade::getAppId, realOffer.getAppId());
            tradeWrapper.ge(Trade::getCreateTime, format.format(oneDayDurD));
            tradeWrapper.like(Trade::getSymbol, "%"+record.getSymbolName()+"%");
            tradeWrapper.orderByAsc(Trade::getId);
            tradeWrapper.last("limit 1");
            List<Trade> oneDayAgoTradeList = tradeMapper.selectList(tradeWrapper);
            BigDecimal oneDayAgoSymbolProfit = new BigDecimal(0);
            Trade oneDayAgoTrade = new Trade();
            if(CollectionUtils.isNotEmpty(oneDayAgoTradeList)){
                oneDayAgoTrade = oneDayAgoTradeList.get(0);
                if(null != oneDayAgoTrade && null != oneDayAgoTrade.getSymbolProfit()){
                    oneDayAgoSymbolProfit = BigDecimal.valueOf(Double.parseDouble(oneDayAgoTrade.getSymbolProfit()));
                    realOfferSymbolResp.setOneDayPhase(String.valueOf(currentSymbolProfit.subtract(oneDayAgoSymbolProfit).setScale(2, RoundingMode.HALF_UP)));
                }
            }
            // 3D盈亏(U)
            long threeDayDur = nowTime-1000*60*60*24*3;
            Date threeDayDurD = new Date(threeDayDur);
            tradeWrapper.clear();
            tradeWrapper.eq(Trade::getAppId, realOffer.getAppId());
            tradeWrapper.ge(Trade::getCreateTime, format.format(threeDayDurD));
            tradeWrapper.like(Trade::getSymbol, "%"+record.getSymbolName()+"%");
            tradeWrapper.orderByAsc(Trade::getId);
            tradeWrapper.last("limit 1");
            List<Trade> threeDayAgoTradeList = tradeMapper.selectList(tradeWrapper);
            BigDecimal threeDayAgoSymbolProfit = new BigDecimal(0);
            BigDecimal threeDayDurSymbolProfit = new BigDecimal(0);
            Trade threeDayAgoTrade = new Trade();
            if(CollectionUtils.isNotEmpty(threeDayAgoTradeList)){
                threeDayAgoTrade = threeDayAgoTradeList.get(0);
                if(null != threeDayAgoTrade && null != threeDayAgoTrade.getSymbolProfit()){
                    threeDayAgoSymbolProfit = BigDecimal.valueOf(Double.parseDouble(threeDayAgoTrade.getSymbolProfit()));
                    threeDayDurSymbolProfit = currentSymbolProfit.subtract(threeDayAgoSymbolProfit);
                    realOfferSymbolResp.setThreeDayPhase(String.valueOf(threeDayDurSymbolProfit.setScale(2, RoundingMode.HALF_UP)));
                }
            }
            // 3D月化(U)
            if(holding.compareTo(BigDecimal.ZERO) > 0){
                BigDecimal threeDayMouthly = new BigDecimal(0);
                threeDayMouthly = threeDayDurSymbolProfit.multiply(BigDecimal.valueOf(10)).divide(holding,4,RoundingMode.HALF_UP);
                realOfferSymbolResp.setThreeDayMonthly(String.valueOf(threeDayMouthly.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)));
            }
            return realOfferSymbolResp;
        }).toList();
        return Result.ok(realOfferSymbolResps);
    }

    public Result<?> getRealOfferSummarySymbolList(Long id) {
        return null;
    }

    public Result<?> getRealOfferSymbolRecordsList(Long id) {
        final LambdaQueryWrapper<RealOfferSymbol> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RealOfferSymbol::getRealOfferId, id);
        return Result.ok(realOfferSymbolMapper.selectList(wrapper));
    }

    public Result<?> getSymbolList(Long id) {
        final LambdaQueryWrapper<RealOfferSymbol> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RealOfferSymbol::getRealOfferId, id);
        List<SymbolListResp> symbolListResps = realOfferSymbolMapper.selectList(wrapper).stream().map(record -> {
            SymbolListResp symbolListResp = new SymbolListResp();
            symbolListResp.setId(record.getId());
            symbolListResp.setName(record.getSymbolName());
            return symbolListResp;
        }).toList();
        return Result.ok(symbolListResps);
    }

    public Result<?> getSymbolTableData(Long id, Long realOfferId) {
        Symbol symbol = symbolMapper.selectById(realOfferSymbolMapper.selectById(id).getSymbolId());
        final LambdaQueryWrapper<RealOfferSymbolExchange> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RealOfferSymbolExchange::getRealOfferSymbolId, id);
        List<RealOfferSymbolExchangeResp> realOfferSymbolExchangeResps = realOfferSymbolExchangeMapper.selectList(wrapper).stream().map(record -> {
            RealOfferSymbolExchangeResp realOfferSymbolExchangeResp = new RealOfferSymbolExchangeResp();
            BeanUtils.copyProperties(record, realOfferSymbolExchangeResp);
            // 币对名称
            realOfferSymbolExchangeResp.setSymbolName(symbol.getName());
            return realOfferSymbolExchangeResp;
        }).toList();
        return Result.ok(realOfferSymbolExchangeResps);
    }
}
