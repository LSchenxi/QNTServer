package com.ninelock.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangePwdReq {
    @NotNull(message = "请输入账号")
    private String account;
    @NotNull(message = "请输入原密码")
    private String oldPassword;
    @NotNull(message = "请输入新密码")
    private String newPassword;
    @NotNull(message = "请输入新密码")
    private String newPasswordTwo;
}
