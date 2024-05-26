package com.ninelock.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author gongym
 * @version 创建时间: 2023-12-16 22:12
 */
@Data
public class DeleteReq {
    @NotNull(message = "ID 不能为空")
    private Long id;
}
