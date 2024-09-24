package com.developer.oss.service;

import com.developer.framework.model.DeveloperResult;
import com.developer.oss.dto.UploadImageDTO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    DeveloperResult<String> uploadFile(MultipartFile file);


    DeveloperResult<UploadImageDTO> uploadImage(MultipartFile file);

}
