package com.ninelock.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author gongym
 * @version 创建时间: 2023-12-15 21:12
 */
@Data
public class UploadFile {
    @NotNull(message = "文件不能为空")
    private MultipartFile file;
}
