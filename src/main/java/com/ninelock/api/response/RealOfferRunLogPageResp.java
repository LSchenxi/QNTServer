package com.ninelock.api.response;

import com.ninelock.api.entity.RealOfferRunLog;
import lombok.Data;

import java.util.List;
@Data
public class RealOfferRunLogPageResp {
    private Long total;
    private List<RealOfferRunLog> records;
}
