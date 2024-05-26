package com.ninelock.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginReq {
    @NotNull(message = "请输入账号")
    private String account;
    @NotNull(message = "请输入密码")
    private String password;
}
