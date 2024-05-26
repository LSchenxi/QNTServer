package com.ninelock.api.controller;

import com.ninelock.api.request.DeleteReq;
import com.ninelock.api.request.StrategyCreateReq;
import com.ninelock.api.request.StrategyReq;
import com.ninelock.api.request.StrategyUpdateReq;
import com.ninelock.api.response.Result;
import com.ninelock.api.service.StrategyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/v1/strategy")
public class StrategyController {

    @Resource
    private StrategyService strategyService;

    /**
     * 获取列表
     */
    @GetMapping("getPage")
    public Result<?> getPage(StrategyReq req){
        return strategyService.getPage(req);
    }

    @GetMapping("getStrategy")
    public Result<?> getStrategy(@RequestParam(name = "id") Long id) {
        return strategyService.getStrategy(id);
    }

    @PostMapping("createStrategy")
    public Result<?> createStrategy(@RequestBody @Validated StrategyCreateReq req) {
        return strategyService.createStrategy(req);
    }

    @PostMapping("updateStrategy")
    public Result<?> updateStrategy(@RequestBody @Validated StrategyUpdateReq req) {
        return strategyService.updateStrategy(req);
    }

    @PostMapping("deleteStrategy")
    public Result<?> deleteStrategy(@RequestBody @Validated DeleteReq req) {
        return strategyService.deleteStrategy(req);
    }
}
