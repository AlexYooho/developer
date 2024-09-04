package com.developer.im.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("test")
public class TestController {


    @RequestMapping("index")
    public String index(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails)authentication.getDetails();
        HashMap<String, String> decodedDetails = (HashMap<String, String>) details.getDecodedDetails();
        String selfUserInfoContext = decodedDetails.get("selfUserInfoKey");
        return "";
    }

}
