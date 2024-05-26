package com.ninelock.api.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.ninelock.api.core.auth.Session;
import com.ninelock.api.request.ChangePwdReq;
import com.ninelock.api.request.LoginReq;
import com.ninelock.api.response.Result;
import com.ninelock.api.service.AuthService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.ninelock.api.core.auth.Session.SESSION_ID;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

/**
 * @author gongym
 * @version 创建时间: 2023-12-13 19:16
 */
@Slf4j
@RestController
@RequestMapping("api/v1/auth")
public class AuthController {

    @Resource
    AuthService authService;

    @GetMapping("check")
    public Result<?> check(HttpServletResponse resp) {
        if (!StpUtil.isLogin()) {
            resp.setStatus(HTTP_UNAUTHORIZED);
            return Result.error401("未登录");
        }
        final Session session = (Session) StpUtil.getSession().get(SESSION_ID);
        log.info("session: {}", session);
        return Result.ok(session);
    }

    @PostMapping("login")
    public Result<?> login(@RequestBody @Validated LoginReq req) {
        return authService.login(req);
    }

    @PostMapping("logout")
    public Result<?> logout() {
        return authService.logout();
    }

    @PostMapping("changePwd")
    public Result<?> changePwd(@RequestBody @Validated ChangePwdReq req) {
        return authService.changePwd(req);
    }
}
