package com.ninelock.api.response;

import lombok.Data;

import java.util.List;

@Data
public class ExchangeDetailPageResp {
    private Long total;
    private List<ExchangeDetailResp> records;
}
