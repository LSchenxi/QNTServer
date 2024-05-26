package com.ninelock.api.controller;

import com.ninelock.api.response.Result;
import com.ninelock.api.service.TestService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/v1/test")
public class TestController {
    @Resource
    private TestService testService;

    @GetMapping("dbTest")
    public Result<?> dbTest() {
        return testService.dbtest();
    }
}
