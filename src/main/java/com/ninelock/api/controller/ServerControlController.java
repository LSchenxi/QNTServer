package com.ninelock.api.controller;

import com.ninelock.api.request.*;
import com.ninelock.api.response.Result;
import com.ninelock.api.service.ServerControlService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/v1/serverControl")
public class ServerControlController {

    @Resource
    private ServerControlService serverControlService;

    /**
     * 获取列表
     */
    @GetMapping("getPage")
    public Result<?> getPage(ServerControlReq req){
        return serverControlService.getPage(req);
    }

    @GetMapping("getServer")
    public Result<?> getServer(@RequestParam(name = "id") Long id) {
        return serverControlService.getServer(id);
    }

    @PostMapping("createServer")
    public Result<?> createServer(@RequestBody @Validated ServerControlCreateReq req) {
        return serverControlService.createServer(req);
    }

    @PostMapping("updateServer")
    public Result<?> updateServer(@RequestBody @Validated ServerControlUpdateReq req) {
        return serverControlService.updateServer(req);
    }

    @PostMapping("deleteServer")
    public Result<?> deleteServer(@RequestBody @Validated DeleteReq req) {
        return serverControlService.deleteServer(req);
    }
}
