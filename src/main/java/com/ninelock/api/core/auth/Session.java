package com.ninelock.api.core.auth;

import lombok.Data;

@Data
public class Session {
    public static final String SESSION_ID = "SESSION_ID";

    private String account;
    private Integer roleCode;
    private Long userId;
}
