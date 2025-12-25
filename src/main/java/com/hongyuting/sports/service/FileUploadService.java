package com.hongyuting.sports.service;

import com.hongyuting.sports.util.FileUtil;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件上传服务
 */
@Getter
@Service
public class FileUploadService {

    @Value("${app.upload-dir:./uploads/}")
    private String uploadDir;
    
    @Autowired
    private FileUtil fileUtil;

    public String uploadImage(MultipartFile file) throws IOException {
        // 使用FileUtil来处理文件上传
        String uniqueFileName = fileUtil.uploadFile(file, "./uploads/");
        return "/uploads/" + uniqueFileName;
    }


    public boolean deleteImage(String imageUrl) {
        try {
            if (imageUrl == null || !imageUrl.startsWith("/uploads/")) {
                return false;
            }

            String filename = imageUrl.substring("/uploads/".length());
            // 使用FileUtil来删除文件
            return fileUtil.deleteFile("./uploads/" + filename);
        } catch (Exception e) {
            return false;
        }
    }

}