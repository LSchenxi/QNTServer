package com.ninelock.api.controller;

import com.ninelock.api.request.DeleteReq;
import com.ninelock.api.request.ExchangeDetailCreateReq;
import com.ninelock.api.request.ExchangeDetailReq;
import com.ninelock.api.request.ExchangeDetailUpdateReq;
import com.ninelock.api.response.Result;
import com.ninelock.api.service.ExchangeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/v1/exchange")
public class ExchangeController {

    @Resource
    private ExchangeService exchangeService;
    /**
     * 获取列表
     */
    @GetMapping("getPage")
    public Result<?> getPage(ExchangeDetailReq req){
        return exchangeService.getPage(req);
    }

    @GetMapping("getExchangeDetail")
    public Result<?> getExchangeDetail(@RequestParam(name = "id") Long id) {
        return exchangeService.getExchangeDetail(id);
    }

    @PostMapping("createExchangeDetail")
    public Result<?> createExchangeDetail(@RequestBody @Validated ExchangeDetailCreateReq req) {
        return exchangeService.createExchangeDetail(req);
    }

    @PostMapping("updateExchangeDetail")
    public Result<?> updateExchangeDetail(@RequestBody @Validated ExchangeDetailUpdateReq req) {
        return exchangeService.updateExchangeDetail(req);
    }

    @PostMapping("deleteExchange")
    public Result<?> deleteExchange(@RequestBody @Validated DeleteReq req) {
        return exchangeService.deleteExchange(req);
    }

    @GetMapping("getProtocol")
    public Result<?> getProtocol() {
        return exchangeService.getProtocol();
    }

    @GetMapping("getExchange")
    public Result<?> getExchange() {
        return exchangeService.getExchange();
    }
}
