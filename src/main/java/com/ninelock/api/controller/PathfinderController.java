package com.ninelock.api.controller;

import com.ninelock.api.request.DetectReq;
import com.ninelock.api.response.Result;
import com.ninelock.api.service.PathfinderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

@Slf4j
@RestController
@RequestMapping("api/v1/pathfinder")
public class PathfinderController {
    @Resource
    private PathfinderService pathfinderService;
    @GetMapping("getDetectIndexList")
    public Result<?> getDetectIndexList() throws SQLException, ClassNotFoundException {
        return pathfinderService.getDetectIndexList();
    }

    @GetMapping("getDetectPage")
    public Result<?> getDetectPage(DetectReq detectReq) throws SQLException, ClassNotFoundException {
        return pathfinderService.getDetectPage(detectReq);
    }

    @GetMapping("getDetectColumns")
    public Result<?> getDetectColumns(Integer currentDetectInfo) throws SQLException, ClassNotFoundException {
        return pathfinderService.getDetectColumns(currentDetectInfo);
    }
}
