package com.developer.sso.config;

import com.alibaba.fastjson.JSON;
import com.developer.framework.model.SelfUserInfoModel;
import com.developer.sso.pojo.UserPO;
import com.developer.sso.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义token负载内容
 */
@Component
public class AccessTokenConvertor extends DefaultAccessTokenConverter {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Map<String, ?> convertAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        Map<String, String> stringMap = (Map<String, String>) super.convertAccessToken(token, authentication);
        if (authentication.getUserAuthentication() == null) {
            return stringMap;
        }

        String username = authentication.getUserAuthentication().getName();
        if (username == null) {
            return stringMap;
        }

        UserPO userInfo = userRepository.findByAccount(username);
        if (userInfo == null) {
            return stringMap;
        }

        SelfUserInfoModel selfUserInfoModel = new SelfUserInfoModel();
        selfUserInfoModel.setUserId(userInfo.getId());
        selfUserInfoModel.setUserName(userInfo.getUsername());
        selfUserInfoModel.setNickName(userInfo.getNickname());
        selfUserInfoModel.setTerminal(0);
        selfUserInfoModel.setEmailAccount(userInfo.getEmail());
        stringMap.put("selfUserInfoKey", JSON.toJSONString(selfUserInfoModel));
        return stringMap;
    }
}
