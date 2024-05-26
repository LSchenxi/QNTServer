package com.ninelock.api.response;

import com.ninelock.api.entity.RealOffer;
import lombok.Data;

@Data
public class RealOfferResp extends RealOffer {
    private String serverIp;
    private String strategyName;
}
