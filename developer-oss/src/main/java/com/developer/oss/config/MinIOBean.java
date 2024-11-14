package com.developer.oss.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class MinIOBean {

    @Resource
    private MinioConfig minioProp;

    @Bean
    public MinioClient minioClient(){
        return MinioClient.builder()
                .endpoint(minioProp.getEndpoint())
                .credentials(minioProp.getAccess(), minioProp.getSecret())
                .build();
    }

}
