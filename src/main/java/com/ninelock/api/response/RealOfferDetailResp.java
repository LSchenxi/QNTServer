package com.ninelock.api.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ninelock.api.entity.RealOffer;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class RealOfferDetailResp extends RealOffer {
    private String strategyName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date strategyUpdateTime;
    private List<String> exchangeNameList;
}
