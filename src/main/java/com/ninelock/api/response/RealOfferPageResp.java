package com.ninelock.api.response;

import lombok.Data;

import java.util.List;

@Data
public class RealOfferPageResp {
    private Long total;
    private List<RealOfferResp> records;
}
