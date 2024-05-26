package com.ninelock.api.response;

import lombok.Data;

@Data
public class SymbolSelectResp {
    private String label;
    private Long value;
    private Integer status;
}
