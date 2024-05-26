package com.ninelock.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServerControlCreateReq {
    @NotNull(message = "服务器ip不能为空")
    private String serverIp;
    @NotNull(message = "操作系统不能为空")
    private String operatingSystem;
    @NotNull(message = "协议不能为空")
    private String protocol;
    @NotNull(message = "版本不能为空")
    private String version;
}
