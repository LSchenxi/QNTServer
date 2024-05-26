package com.ninelock.api.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ninelock.api.core.auth.Session;
import com.ninelock.api.entity.RealOffer;
import com.ninelock.api.entity.Server;
import com.ninelock.api.mapper.RealOfferMapper;
import com.ninelock.api.mapper.ServerMapper;
import com.ninelock.api.request.*;
import com.ninelock.api.response.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;

import static com.ninelock.api.core.auth.Session.SESSION_ID;
import static com.ninelock.api.core.constant.EntityConstant.IS_EXIST;

@Slf4j
@Service
public class ServerControlService extends ServiceImpl<ServerMapper, Server> {

    @Resource
    private RealOfferMapper realOfferMapper;

    public Result<?> getPage(ServerControlReq serverControlReq) {
        final Session session = (Session) StpUtil.getSession().get(SESSION_ID);
        Long userId = session.getUserId();
        final int page = serverControlReq.getPage();
        final int size = serverControlReq.getSize();

        // 查询分页列表
        final LambdaQueryWrapper<Server> wrapper = new LambdaQueryWrapper<>();
        if(null!=serverControlReq.getServerIp() && !"".equals(serverControlReq.getServerIp())){
            wrapper.eq(Server::getServerIp, serverControlReq.getServerIp());
        }
        wrapper.eq(Server::getDelFlag, IS_EXIST);
        wrapper.eq(Server::getCreateId, userId);
        final Page<Server> serverPage = this.page(new Page<>(page, size), wrapper);
        // 转为响应对象
        final List<ServerControlResp> serverControlRespList = serverPage.getRecords().stream().map(record -> {
            final ServerControlResp serverControlResp = new ServerControlResp();
            BeanUtils.copyProperties(record, serverControlResp);
            // 发布时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if(null != record.getUpdateTime()){
                serverControlResp.setReleaseTime(sdf.format(record.getUpdateTime()));
            } else {
                serverControlResp.setReleaseTime("");
            }
            // 实盘数量
            final LambdaQueryWrapper<RealOffer> realOfferWrapper = new LambdaQueryWrapper<>();
            realOfferWrapper.eq(RealOffer::getServerId, record.getId());
            realOfferWrapper.eq(RealOffer::getDelFlag, IS_EXIST);
            serverControlResp.setRealOfferNumber(realOfferMapper.selectCount(realOfferWrapper).intValue());
            return serverControlResp;
        }).toList();
        final ServerControlPageResp serverControlPageResp = new ServerControlPageResp();
        serverControlPageResp.setTotal(serverPage.getTotal());
        serverControlPageResp.setRecords(serverControlRespList);
        return Result.ok(serverControlPageResp);
    }

    public Result<?> getServer(Long id) {
        return Result.ok(this.baseMapper.selectById(id));
    }

    public Result<?> createServer(ServerControlCreateReq req) {
        final Session session = (Session) StpUtil.getSession().get(SESSION_ID);
        Long userId = session.getUserId();
        final LambdaQueryWrapper<Server> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Server::getServerIp, req.getServerIp());
        wrapper.eq(Server::getDelFlag, IS_EXIST);
        wrapper.eq(Server::getCreateId, userId);
        long count = this.count(wrapper);
        if(count > 0){
            return Result.error("ip地址重复", null);
        }
        Server server = new Server();
        BeanUtils.copyProperties(req, server);
        server.setStatus("发布");
        server.setPosition("瑞士 苏黎世");
        server.setCreateId(userId);
        server.setRealOfferNumber(0);
        if(this.save(server)){
            return Result.ok();
        }else {
            return Result.error("保存失败", null);
        }
    }

    public Result<?> updateServer(ServerControlUpdateReq req) {
        final LambdaQueryWrapper<Server> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Server::getServerIp, req.getServerIp());
        wrapper.eq(Server::getDelFlag, IS_EXIST);
        wrapper.ne(Server::getId, req.getId());
        long count = this.count(wrapper);
        if(count > 0){
            return Result.error("ip地址重复", null);
        }
        Server server = new Server();
        BeanUtils.copyProperties(req, server);
        if (this.updateById(server)) {
            return Result.ok();
        } else {
            return Result.error("更新失败", null);
        }
    }

    public Result<?> deleteServer(DeleteReq req) {
        final LambdaQueryWrapper<RealOffer> realOfferWrapper = new LambdaQueryWrapper<>();
        realOfferWrapper.eq(RealOffer::getServerId, req.getId());
        realOfferWrapper.eq(RealOffer::getDelFlag, IS_EXIST);
        Long aLong = realOfferMapper.selectCount(realOfferWrapper);
        if(aLong > 0L){
            return Result.error("删除失败，此服务器尚有运行中实盘", null);
        }
        if (this.removeById(req.getId())) {
            return Result.ok();
        } else {
            return Result.error("删除失败", null);
        }
    }
}
