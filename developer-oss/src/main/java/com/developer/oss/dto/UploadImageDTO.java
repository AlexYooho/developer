package com.developer.oss.dto;

import lombok.Data;

@Data
public class UploadImageDTO {

    /**
     * 原图
     */
    private String originUrl;

    /**
     * 缩略图
     */
    private String thumbUrl;

}
