package com.ninelock.api.controller;

import com.ninelock.api.request.RealOfferRevenueChartDataReq;
import com.ninelock.api.response.Result;
import com.ninelock.api.service.DataOverviewService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/v1/dataOverview")
public class DataOverviewController {

    @Resource
    private DataOverviewService dataOverviewService;

    @GetMapping("getRealOfferList")
    public Result<?> getRealOfferList() {
        return dataOverviewService.getRealOfferList();
    }

    @GetMapping("getAccountProfitChartData")
    public Result<?> getAccountProfitChartData(RealOfferRevenueChartDataReq req) {
        return dataOverviewService.getAccountProfitChartData(req);
    }

    @GetMapping("getAccountStrategyChartData")
    public Result<?> getAccountStrategyChartData(RealOfferRevenueChartDataReq req) {
        return dataOverviewService.getAccountStrategyChartData(req);
    }

}
