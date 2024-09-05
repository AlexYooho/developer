package com.developer.message.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("message")
public class messageController {


    @GetMapping("index")
    public String index(){
        return "Hello world";
    }


}
