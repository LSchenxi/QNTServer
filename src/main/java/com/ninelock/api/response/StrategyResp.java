package com.ninelock.api.response;

import com.ninelock.api.entity.Strategy;
import lombok.Data;

@Data
public class StrategyResp extends Strategy {
    private String expirationTimeStr;
    private String createTimeStr;
}
