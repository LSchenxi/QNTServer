package com.ninelock.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DetectReq {
    /**
     * 页码
     */
    @NotNull(message = "页码不能为空")
    private Integer page;

    /**
     * 每页数量
     */
    @NotNull(message = "每页数量不能为空")
    private Integer size;

    /**
     * detectIndex表id
     */
    @NotNull(message = "index不能为空")
    private Integer currentDetectInfo;

    /**
     * 币种名称
     */
    private String symbolName;
}
