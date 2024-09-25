package com.developer.sso.service;

import com.developer.framework.model.DeveloperResult;
import com.developer.sso.client.OAuthClient;
import com.developer.sso.dto.LoginDTO;
import com.developer.sso.dto.TokenDTO;
import com.developer.sso.model.UserInfo;
import com.developer.sso.pojo.UserPO;
import com.developer.sso.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OAuthClient oAuthClient;

    @Override
    public UserInfo getUserByUserName(String userName) {

        UserInfo userInfo = new UserInfo();

        // 查询数据库用户是否存在
        UserPO user = userRepository.findByAccount(userName);
        userInfo.setUserId(user.getId().toString());
        userInfo.setPassword(user.getPassword());
        userInfo.setUsername(userName);

        return userInfo;
    }

    @Override
    public DeveloperResult<TokenDTO> Login(LoginDTO dto) {
        try {
            Map<String, Object> response = oAuthClient.getToken("password", dto.getAccount(), dto.getPassword(), "client_dev", "dev");
            TokenDTO tokenDTO = TokenDTO.builder().accessToken(response.get("access_token").toString()).refreshToken(response.get("refresh_token").toString()).build();
            return DeveloperResult.success(tokenDTO);
        }catch (FeignException e){
            String errorMsg = parseAndHandleError(e.contentUTF8());
            return DeveloperResult.error(e.status(),errorMsg);
        }
    }

    private String parseAndHandleError(String responseBody) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map errorResponse = mapper.readValue(responseBody, Map.class);

            return (String) errorResponse.get("error_description");
        } catch (Exception ex) {
            System.err.println("Failed to parse error response: " + ex.getMessage());
        }
        return null;
    }
}
