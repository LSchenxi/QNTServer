package com.ninelock.api.response;

import lombok.Data;

@Data
public class ExchangeSelectResp {
    private String label;
    private Long value;
    private Boolean disabled;
}
