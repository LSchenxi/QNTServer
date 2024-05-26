package com.ninelock.api.response;

import lombok.Data;

@Data
public class RealOfferLogResp {
    private String logTime;
    private String logType;
    private String logInfo;
    private Integer profitFlag;
}
