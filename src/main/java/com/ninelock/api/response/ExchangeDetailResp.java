package com.ninelock.api.response;

import com.ninelock.api.entity.ExchangeDetail;
import lombok.Data;

@Data
public class ExchangeDetailResp extends ExchangeDetail {
    private String exchangeName;
}
