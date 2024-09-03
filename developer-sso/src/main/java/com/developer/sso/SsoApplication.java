package com.developer.sso;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan("com.developer")
@MapperScan("com.developer.sso.mappers")
public class SsoApplication {


    public static void main(String[] args) {
        SpringApplication.run(SsoApplication.class,args);
    }

}
