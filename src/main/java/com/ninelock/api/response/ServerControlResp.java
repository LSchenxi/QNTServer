package com.ninelock.api.response;

import com.ninelock.api.entity.Server;
import lombok.Data;

@Data
public class ServerControlResp extends Server {
    private String releaseTime;
}
