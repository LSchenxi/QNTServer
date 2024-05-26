package com.ninelock.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExchangeDetailCreateReq {
    @NotNull(message = "协议类型不能为空")
    private Long protocolId;
    @NotNull(message = "交易所不能为空")
    private Long exchangeId;
    @NotNull(message = "账户access key不能为空")
    private String accessKey;
    @NotNull(message = "账户secret key不能为空")
    private String secretKey;
    private String passphrase;
    private Integer v5Flag;
    private Integer v3Flag;
    private String version;
    private String password;
    @NotNull(message = "交易所标签/名称不能为空")
    private String exchangeLabel;
}
