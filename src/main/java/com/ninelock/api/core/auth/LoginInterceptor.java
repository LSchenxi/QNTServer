package com.ninelock.api.core.auth;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import io.micrometer.common.lang.NonNullApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Map;

import static com.ninelock.api.core.auth.Session.SESSION_ID;

/**
 * @author gongym
 * @version 创建时间: 2023-12-13 18:35
 */
@Slf4j
@Component
@NonNullApi
public class LoginInterceptor implements HandlerInterceptor {
    private final List<String> whiteList = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/check",
            "/api/v1/auth/logout",
            "/api/v1/test/dbTest"
    );

    private final List<String> userManagementApis = List.of(
            "/api/v1/userManagement/getPage",
            "/api/v1/userManagement/getUser",
            "/api/v1/userManagement/createUser",
            "/api/v1/userManagement/updateUser",
            "/api/v1/userManagement/deleteUser",
            "/api/v1/userManagement/resetUserPwd"
    );

    private boolean checkWhiteList(String uri) {
        for (String white : whiteList) {
            if (white.equals(uri)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkRoleRoute(String uri) {
        final Session account = (Session) StpUtil.getSession().get(SESSION_ID);
        if (account == null) {
            return false;
        }
        if (ObjectUtil.equal(account.getRoleCode(), 0)) {
            return true;
        } else if(ObjectUtil.equal(account.getRoleCode(), 1)){
            if(userManagementApis.contains(uri)){
                return false;
            }
            return true;
        }else {
            return true;
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        // 如果是白名单，则放行
        if (checkWhiteList(uri)) {
            return true;
        }

        try {
            // 判断是否登录
            if (!StpUtil.isLogin()) {
                response.setStatus(401);
                return false;
            }
            // 判断是否有权限
            if (!checkRoleRoute(uri)) {
                log.error("没有访问当前接口的权限 {}", uri);
                response.setStatus(401);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("登录验证失败", e);
            response.setStatus(401);
            return false;
        }
    }
}
