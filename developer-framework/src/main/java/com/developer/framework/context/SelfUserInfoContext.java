package com.developer.framework.context;

import com.alibaba.fastjson.JSON;
import com.developer.framework.model.SelfUserInfoModel;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

import java.util.HashMap;

public class SelfUserInfoContext {

    public static SelfUserInfoModel selfUserInfo(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails)authentication.getDetails();
        HashMap<String, String> decodedDetails = (HashMap<String, String>) details.getDecodedDetails();
        String selfUserInfoContext = decodedDetails.get("selfUserInfoKey");
        return JSON.parseObject(selfUserInfoContext, SelfUserInfoModel.class);
    }

}
