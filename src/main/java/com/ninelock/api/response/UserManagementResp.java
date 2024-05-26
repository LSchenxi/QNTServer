package com.ninelock.api.response;

import com.ninelock.api.entity.User;
import lombok.Data;

@Data
public class UserManagementResp extends User{
    private String userTypeString;
}
