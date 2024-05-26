package com.ninelock.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserUpdateReq {

    @NotNull(message = "用户ID 不能为空")
    private Long id;
    @NotNull(message = "用户名不能为空")
    private String account;
    @NotNull(message = "用户类型不能为空")
    private Integer userType;
}
