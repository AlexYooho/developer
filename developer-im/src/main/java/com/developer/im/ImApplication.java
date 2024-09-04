package com.developer.im;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan("com.developer")
public class ImApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImApplication.class,args);
    }

}
