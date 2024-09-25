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
        // 需要查询相关用户信息放在jwt中
        HashMap<String, String> details = (HashMap<String, String>) authentication.getUserAuthentication().getDetails();

        UserPO userInfo = userRepository.findByAccount(details.get("username"));

        SelfUserInfoModel selfUserInfoModel = new SelfUserInfoModel();
        selfUserInfoModel.setUserId(userInfo.getId());
        selfUserInfoModel.setUserName(userInfo.getUsername());
        selfUserInfoModel.setNickName(userInfo.getNickname());
        selfUserInfoModel.setTerminal(0);
        String jsonString = JSON.toJSONString(selfUserInfoModel);


        stringMap.put("selfUserInfoKey",jsonString);

        return stringMap;
    }
}
