package com.developer.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Controller
@RequestMapping("/demo")
@RefreshScope
public class DemoController {


    @Value("${server.port}")
    private Integer port;

    @RequestMapping("/index")
    public String Index(){
        return "nacos获取内容:"+port;
    }


}
