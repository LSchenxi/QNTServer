package com.ninelock.api.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ninelock.api.entity.User;
import com.ninelock.api.mapper.UserMapper;
import com.ninelock.api.request.DeleteReq;
import com.ninelock.api.request.UserCreateReq;
import com.ninelock.api.request.UserManagementReq;
import com.ninelock.api.request.UserUpdateReq;
import com.ninelock.api.response.Result;
import com.ninelock.api.response.UserManagementPageResp;
import com.ninelock.api.response.UserManagementResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.ninelock.api.core.constant.EntityConstant.IS_EXIST;

@Slf4j
@Service
public class UserManagementService extends ServiceImpl<UserMapper, User> {

    public Result<?> getPage(UserManagementReq userManagementReq) {
        final int page = userManagementReq.getPage();
        final int size = userManagementReq.getSize();

        // 查询分页列表
        final LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>();
        if(null!=userManagementReq.getUserAccount() && !"".equals(userManagementReq.getUserAccount())){
            wrapper.like(User::getAccount, userManagementReq.getUserAccount());
        }
        wrapper.eq(User::getDelFlag, IS_EXIST);
        final Page<User> userPage = this.page(new Page<>(page, size), wrapper);
        // 转为响应对象
        final List<UserManagementResp> userManagementRespList = userPage.getRecords().stream().map(record -> {
            final UserManagementResp userManagementResp = new UserManagementResp();
            BeanUtils.copyProperties(record, userManagementResp);

            // 性别（1：男；2：女）
            if(record.getUserType() == 1){
                userManagementResp.setUserTypeString("普通用户");
            } else if (record.getUserType() == 0){
                userManagementResp.setUserTypeString("管理员");
            }
            return userManagementResp;
        }).toList();
        final UserManagementPageResp userManagementPageResp = new UserManagementPageResp();
        userManagementPageResp.setTotal(userPage.getTotal());
        userManagementPageResp.setRecords(userManagementRespList);
        return Result.ok(userManagementPageResp);
    }

    public Result<?> getUser(Long id) {
        return Result.ok(this.baseMapper.selectById(id));
    }

    public Result<?> createUser(UserCreateReq req) {
        final LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>();
        wrapper.eq(User::getAccount, req.getAccount());
        wrapper.eq(User::getDelFlag, IS_EXIST);
        long count = this.count(wrapper);
        if(count > 0){
            return Result.error("用户名重复", null);
        }
        User user = new User();
        BeanUtils.copyProperties(req, user);
        user.setPassword("123456");
        if(this.save(user)){
            return Result.ok();
        }else {
            return Result.error("保存失败", null);
        }
    }

    public Result<?> updateUser(UserUpdateReq req) {
        final LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>();
        wrapper.eq(User::getAccount, req.getAccount());
        wrapper.eq(User::getDelFlag, IS_EXIST);
        wrapper.ne(User::getId, req.getId());
        long count = this.count(wrapper);
        if(count > 0){
            return Result.error("用户名重复", null);
        }
        User user = new User();
        BeanUtils.copyProperties(req, user);
        if (this.updateById(user)) {
            return Result.ok();
        } else {
            return Result.error("更新失败", null);
        }
    }

    public Result<?> deleteUser(DeleteReq req) {
        if (this.removeById(req.getId())) {
            return Result.ok();
        } else {
            return Result.error("删除失败", null);
        }
    }

    public Result<?> resetUserPwd(DeleteReq req) {
        User user = new User();
        user.setId(req.getId());
        user.setPassword("123456");
        if (this.updateById(user)) {
            return Result.ok();
        } else {
            return Result.error("更新失败", null);
        }
    }
}
