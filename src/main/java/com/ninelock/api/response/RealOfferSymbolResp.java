package com.ninelock.api.response;

import com.ninelock.api.entity.RealOfferSymbol;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RealOfferSymbolResp extends RealOfferSymbol {
    private String delayTimeConsumingPolling;
}
