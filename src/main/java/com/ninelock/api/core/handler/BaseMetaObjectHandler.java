package com.ninelock.api.core.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.ninelock.api.core.constant.EntityConstant.IS_EXIST;

/**
 * @author gongym
 * @version 创建时间: 2023-12-13 16:29
 */
@Component
public class BaseMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        Date currentTime = new Date();

        this.setFieldValByName("createTime", currentTime, metaObject);
        this.setFieldValByName("updateTime", currentTime, metaObject);
        this.setFieldValByName("delFlag", IS_EXIST, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Date currentTime = new Date();

        this.setFieldValByName("updateTime", currentTime, metaObject);
        this.setFieldValByName("delFlag", IS_EXIST, metaObject);
    }
}
