package com.ninelock.api.response;

import lombok.Data;

@Data
public class QntDetectResponse {
    private String message;
    private Object data;
    private Boolean success;
    private Integer code;
    private Long timestamp;
}
