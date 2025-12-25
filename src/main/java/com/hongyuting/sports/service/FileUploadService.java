package com.hongyuting.sports.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件上传服务接口
 */
public interface FileUploadService {
    /**
     * 上传图片
     */
    String uploadImage(MultipartFile file) throws IOException;

    /**
     * 删除图片
     */
    void deleteImage(String imageUrl);
}
