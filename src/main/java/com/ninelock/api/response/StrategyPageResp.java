package com.ninelock.api.response;

import lombok.Data;

import java.util.List;

@Data
public class StrategyPageResp {
    private Long total;
    private List<StrategyResp> records;
}
