package com.ninelock.api.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ninelock.api.core.auth.CoreService;
import com.ninelock.api.core.auth.Session;
import com.ninelock.api.entity.User;
import com.ninelock.api.mapper.UserMapper;
import com.ninelock.api.request.ChangePwdReq;
import com.ninelock.api.request.LoginReq;
import com.ninelock.api.response.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.ninelock.api.core.auth.Session.SESSION_ID;
import static com.ninelock.api.core.constant.EntityConstant.IS_EXIST;


/**
 * @author gongym
 * @version 创建时间: 2023-12-13 19:39
 */
@Slf4j
@Service
public class AuthService {

    @Resource
    private CoreService coreService;
    @Resource
    private UserMapper userMapper;

    public Result<?> login(LoginReq req) {
        final LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getDelFlag, IS_EXIST).eq(User::getAccount, req.getAccount());
        final List<User> userList = userMapper.selectList(userLambdaQueryWrapper);
        if(CollectionUtils.isEmpty(userList)){
            return Result.error401("用户不存在");
        }
        final User user = userList.get(0);
        if(!req.getPassword().equals(user.getPassword())){
            return Result.error401("密码错误");
        }
        final Session session = new Session();
        session.setUserId(user.getId());
        session.setAccount(user.getAccount());
        session.setRoleCode(user.getUserType());
        StpUtil.login(user.getAccount());
        StpUtil.getSession().set(SESSION_ID, session);
        return Result.ok(session);
    }

    public Result<?> logout() {
        StpUtil.logout();
        return Result.ok();
    }

    public Result<?> changePwd(ChangePwdReq req) {
        final LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getDelFlag, IS_EXIST).eq(User::getAccount, req.getAccount());
        final List<User> userList = userMapper.selectList(userLambdaQueryWrapper);
        if(CollectionUtils.isEmpty(userList)){
            return Result.error401("用户不存在");
        }
        final User user = userList.get(0);
        if(!req.getOldPassword().equals(user.getPassword())){
            return Result.error401("原密码错误");
        }
        user.setPassword(req.getNewPassword());
        userMapper.updateById(user);
        return Result.ok();
    }
}
