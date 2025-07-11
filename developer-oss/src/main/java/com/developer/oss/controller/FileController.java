package com.developer.oss.controller;

import com.developer.framework.model.DeveloperResult;
import com.developer.oss.dto.UploadImageDTO;
import com.developer.oss.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("oss")
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * 上传图片
     * @param file
     * @return
     */
    @PostMapping("/image/upload")
    public DeveloperResult<UploadImageDTO> uploadImage(@RequestParam("file") MultipartFile file){
        return fileService.uploadImage(file);
    }

    /**
     * 上传文件
     * @param file
     * @return
     */
    @PostMapping("/file/upload")
    public DeveloperResult<String> uploadFile(MultipartFile file){
        return fileService.uploadFile(file);
    }

}
