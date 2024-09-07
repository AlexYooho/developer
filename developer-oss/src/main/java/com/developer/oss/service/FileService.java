package com.developer.oss.service;

import com.developer.framework.model.DeveloperResult;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    DeveloperResult uploadFile(MultipartFile file);


    DeveloperResult uploadImage(MultipartFile file);

}
