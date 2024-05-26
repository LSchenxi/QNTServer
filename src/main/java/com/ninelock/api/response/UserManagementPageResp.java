package com.ninelock.api.response;

import lombok.Data;

import java.util.List;

@Data
public class UserManagementPageResp {
    private Long total;
    private List<UserManagementResp> records;
}
