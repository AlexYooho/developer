package com.developer.oss.service.impl;

import com.developer.oss.enums.FileTypeEnum;
import com.developer.oss.service.MinioService;
import com.developer.oss.util.MinioUtils;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class MinioServiceImpl implements MinioService {

    @Autowired
    private MinioUtils minioUtils;

    @Override
    public Boolean bucketExists(String bucketName) {
        return minioUtils.bucketExists(bucketName);
    }

    @Override
    public void createBucket(String bucketName) {
        minioUtils.makeBucket(bucketName);
    }

    @SneakyThrows
    @Override
    public List<Object> findAllObjectByBucketName(String bucketName) {
        return minioUtils.findAllObjectByBucketName(bucketName);
    }

    @Override
    public String uploadFile(String bucketName,String fileType,MultipartFile multipartFile) {
        return minioUtils.uploadFile(bucketName,fileType,multipartFile);
    }

    @Override
    public void batchUploadFile(String bucketName,MultipartFile[] multipartFile) {
        minioUtils.batchUploadFile(bucketName,multipartFile);
    }

    @Override
    public Boolean delFile(String bucketName,String fileName) {
        return minioUtils.deleteObject(bucketName, fileName);
    }

    @Override
    public String findFileUrl(String bucketName,FileTypeEnum fileType, String fileName) {
        return minioUtils.findFileUrl(bucketName,fileType,fileName);
    }
}
