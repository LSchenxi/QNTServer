package com.ninelock.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class UserCreateReq{
    @NotNull(message = "用户名不能为空")
    private String account;
    @NotNull(message = "用户类型不能为空")
    private Integer userType;
}
