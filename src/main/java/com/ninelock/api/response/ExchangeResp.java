package com.ninelock.api.response;

import com.ninelock.api.entity.Exchange;
import lombok.Data;

@Data
public class ExchangeResp extends Exchange {
    private Integer status;
}
