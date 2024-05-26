package com.ninelock.api.response;

import lombok.Data;

import java.util.List;

@Data
public class RealOfferLogPageResp {
    private Long total;
    private List<RealOfferLogResp> records;
}
