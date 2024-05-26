package com.ninelock.api.response;

import com.ninelock.api.entity.RealOfferExchange;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RealOfferExchangeResp extends RealOfferExchange {
    private String exchangeName;
    private String exchangeStatus;
}
