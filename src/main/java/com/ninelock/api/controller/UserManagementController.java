package com.ninelock.api.controller;

import com.ninelock.api.request.DeleteReq;
import com.ninelock.api.request.UserCreateReq;
import com.ninelock.api.request.UserManagementReq;
import com.ninelock.api.request.UserUpdateReq;
import com.ninelock.api.service.UserManagementService;
import com.ninelock.api.response.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/v1/userManagement")
public class UserManagementController {
    @Resource
    private UserManagementService userManagementService;

    /**
     * 获取列表
     */
    @GetMapping("getPage")
    public Result<?> getPage(UserManagementReq req){
        return userManagementService.getPage(req);
    }

    @GetMapping("getUser")
    public Result<?> getUser(@RequestParam(name = "id") Long id) {
        return userManagementService.getUser(id);
    }

    @PostMapping("createUser")
    public Result<?> createUser(@RequestBody @Validated UserCreateReq req) {
        return userManagementService.createUser(req);
    }

    @PostMapping("updateUser")
    public Result<?> updateUser(@RequestBody @Validated UserUpdateReq req) {
        return userManagementService.updateUser(req);
    }

    @PostMapping("deleteUser")
    public Result<?> deleteUser(@RequestBody @Validated DeleteReq req) {
        return userManagementService.deleteUser(req);
    }

    @PostMapping("resetUserPwd")
    public Result<?> resetUserPwd(@RequestBody @Validated DeleteReq req) {
        return userManagementService.resetUserPwd(req);
    }
}
