package com.ninelock.api.response;

import lombok.Data;

import java.util.List;

@Data
public class ServerControlPageResp {
    private Long total;
    private List<ServerControlResp> records;
}
