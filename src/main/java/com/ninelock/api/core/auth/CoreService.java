package com.ninelock.api.core.auth;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.ninelock.api.core.auth.Session.SESSION_ID;

@Service
public class CoreService {

    public String encodePassword(String origin) {
        final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(origin);
    }

    public boolean matchPassword(String origin, String encode) {
        final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(origin, encode);
    }

    public SaTokenInfo doLogin(String account, Session sessionInfo) {
        // 执行登录
        StpUtil.login(account);

        // 保存登录用户信息
        final SaSession session = StpUtil.getSession();
        session.set(SESSION_ID, sessionInfo);

        // 返回token信息
        return StpUtil.getTokenInfo();
    }

}
