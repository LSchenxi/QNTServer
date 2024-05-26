package com.ninelock.api.response;

import lombok.Data;

@Data
public class QntRobotResponse {
    private String msg;
    private Object data;
    private Boolean success;
}
