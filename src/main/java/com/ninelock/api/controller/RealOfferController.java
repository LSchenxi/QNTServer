package com.ninelock.api.controller;

import com.ninelock.api.request.*;
import com.ninelock.api.response.Result;
import com.ninelock.api.service.RealOfferService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/v1/realOffer")
public class RealOfferController {
    @Resource
    private RealOfferService realOfferService;

    /**
     * 获取列表
     */
    @GetMapping("getPage")
    public Result<?> getPage(RealOfferReq req){
        return realOfferService.getPage(req);
    }

    @GetMapping("getRealOffer")
    public Result<?> getRealOffer(@RequestParam(name = "id") Long id) {
        return realOfferService.getRealOffer(id);
    }

    @PostMapping("createRealOffer")
    public Result<?> createRealOffer(@RequestBody @Validated RealOfferCreateReq req) {
        return realOfferService.createRealOffer(req);
    }

    @PostMapping("updateRealOffer")
    public Result<?> updateRealOffer(@RequestBody @Validated RealOfferBaseUpdateReq req) {
        //接口废弃 实盘列表暂无更新功能
        return realOfferService.updateRealOffer(req);
    }

    @PostMapping("deleteRealOffer")
    public Result<?> deleteRealOffer(@RequestBody @Validated DeleteReq req) {
        return realOfferService.deleteRealOffer(req);
    }

    @PostMapping("stopRealOffer")
    public Result<?> stopRealOffer(@RequestBody @Validated DeleteReq req) {
        return realOfferService.stopRealOffer(req);
    }

    @PostMapping("startRealOffer")
    public Result<?> startRealOffer(@RequestBody @Validated DeleteReq req) {
        return realOfferService.startRealOffer(req);
    }

    @GetMapping("getServerList")
    public Result<?> getServerList() {
        return realOfferService.getServerList();
    }

    @GetMapping("getStrategyList")
    public Result<?> getStrategyList() {
        return realOfferService.getStrategyList();
    }

    @GetMapping("getExchangeList")
    public Result<?> getExchangeList() {
        return realOfferService.getExchangeList();
    }

    @GetMapping("getTradingCurrencyList")
    public Result<?> getTradingCurrencyList() {
        //已废弃 原获取币list接口
        return realOfferService.getTradingCurrencyList();
    }
}
