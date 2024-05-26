package com.ninelock.api.controller;

import com.ninelock.api.request.*;
import com.ninelock.api.response.Result;
import com.ninelock.api.service.RealOfferDetailService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/v1/realOfferDetail")
public class RealOfferDetailController {
    @Resource
    private RealOfferDetailService realOfferDetailService;

    @GetMapping("getRealOffer")
    public Result<?> getRealOffer(@RequestParam(name = "realOfferId") Long realOfferId) {
        return realOfferDetailService.getRealOffer(realOfferId);
    }

    @PostMapping("updateRealOfferBase")
    public Result<?> updateRealOffer(@RequestBody @Validated RealOfferBaseUpdateReq req) {
        return realOfferDetailService.updateRealOfferBase(req);
    }

    @PostMapping("updateRealOfferAutoTrade")
    public Result<?> updateRealOfferAutoTrade(@RequestBody @Validated RealOfferBaseAutoTradeUpdateReq req) {
        return realOfferDetailService.updateRealOfferAutoTrade(req);
    }

    @PostMapping("updateRealOfferTradeParam")
    public Result<?> updateRealOfferTradeParam(@RequestBody @Validated RealOfferTradeParamUpdateReq req) {
        return realOfferDetailService.updateRealOfferTradeParam(req);
    }

    @GetMapping("getAllExchange")
    public Result<?> getAllExchange(@RequestParam(name = "id") Long id) {
        return realOfferDetailService.getAllExchange(id);
    }

    @GetMapping("getAllSymbolList")
    public Result<?> getAllSymbolList() {
        return realOfferDetailService.getAllSymbolList();
    }

    @GetMapping("getRealOfferSymbolList")
    public Result<?> getRealOfferCurrencyList(@RequestParam(name = "id") Long id) {
        return realOfferDetailService.getRealOfferSymbolList(id);
    }

    @GetMapping("getCurrentSymbolExchangeList")
    public Result<?> getCurrentSymbolExchangeList(@RequestParam(name = "symbolId") Long symbolId, @RequestParam(name = "realOfferId") Long realOfferId) {
        return realOfferDetailService.getCurrentSymbolExchangeList(symbolId, realOfferId);
    }

    @PostMapping("updateSymbolExchange")
    public Result<?> updateSymbolExchange(@RequestBody @Validated SymbolExchangeUpdataReq req) {
        return realOfferDetailService.updateSymbolExchange(req);
    }

    @PostMapping("updateAddSymbol")
    public Result<?> updateAddSymbol(@RequestBody @Validated AddCurrencyToRealOfferReq req) throws Exception {
        return realOfferDetailService.updateAddSymbol(req);
    }

    @GetMapping("getRealOfferAllSymbol")
    public Result<?> getRealOfferAllSymbol(@RequestParam(name = "id") Long id) {
        return realOfferDetailService.getRealOfferAllSymbol(id);
    }

    @GetMapping("getRealOfferStartingSymbol")
    public Result<?> getRealOfferStartingSymbol(@RequestParam(name = "id") Long id) {
        return realOfferDetailService.getRealOfferStartingSymbol(id);
    }

    @PostMapping("updateStartSymbol")
    public Result<?> updateStartSymbol(@RequestBody @Validated AddCurrencyToRealOfferReq req) throws Exception {
        return realOfferDetailService.updateStartSymbol(req);
    }

    @GetMapping("getRealOfferRevenueChartData")
    public Result<?> getRealOfferRevenueChartData(RealOfferRevenueChartDataReq req) {
        return realOfferDetailService.getRealOfferRevenueChartData(req);
    }

    @GetMapping("getRealOfferStrategyChartData")
    public Result<?> getRealOfferStrategyChartData(RealOfferRevenueChartDataReq req) {
        return realOfferDetailService.getRealOfferStrategyChartData(req);
    }

    @GetMapping("getLogTableData")
    public Result<?> getLogTableData(RealOfferLogReq req) {
        return realOfferDetailService.getLogTableData(req);
    }

    @GetMapping("getLogTableData2")
    public Result<?> getLogTableData2(RealOfferLogReq req) {
        return realOfferDetailService.getLogTableData2(req);
    }

    @GetMapping("getRealOfferExchangeList")
    public Result<?> getRealOfferExchangeList(Long id) {
        return realOfferDetailService.getRealOfferExchangeList(id);
    }

    @GetMapping("getRealOfferEnableSymbolList")
    public Result<?> getRealOfferEnableSymbolList(Long id) {
        return realOfferDetailService.getRealOfferEnableSymbolList(id);
    }

    @GetMapping("getRealOfferClearingSymbolList")
    public Result<?> getRealOfferClearingSymbolList(Long id) {
        return realOfferDetailService.getRealOfferClearingSymbolList(id);
    }

    @GetMapping("getRealOfferLockingSymbolList")
    public Result<?> getRealOfferLockingSymbolList(Long id) {
        return realOfferDetailService.getRealOfferLockingSymbolList(id);
    }

    @GetMapping("getRealOfferSuspendSymbolList")
    public Result<?> getRealOfferSuspendSymbolList(Long id) {
        return realOfferDetailService.getRealOfferSuspendSymbolList(id);
    }

    @GetMapping("getRealOfferSummarySymbolList")
    public Result<?> getRealOfferSummarySymbolList(Long id) {
        return realOfferDetailService.getRealOfferSummarySymbolList(id);
    }

    @GetMapping("getRealOfferSymbolRecordsList")
    public Result<?> getRealOfferSymbolRecordsList(Long id) {
        return realOfferDetailService.getRealOfferSymbolRecordsList(id);
    }

    @GetMapping("getSymbolList")
    public Result<?> getSymbolList(Long id) {
        return realOfferDetailService.getSymbolList(id);
    }

    @GetMapping("getSymbolTableData")
    public Result<?> getSymbolTableData(@RequestParam(name = "id") Long id, @RequestParam(name = "realOfferId") Long realOfferId) {
        return realOfferDetailService.getSymbolTableData(id, realOfferId);
    }

}
