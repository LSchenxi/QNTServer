package com.ninelock.api.response;

import com.ninelock.api.entity.RealOfferSymbolExchange;
import lombok.Data;

@Data
public class RealOfferSymbolExchangeResp extends RealOfferSymbolExchange {
    private String symbolName;
}
