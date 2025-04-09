package com.developer.sso.service;

import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.SnowflakeNoUtil;
import com.developer.sso.client.OAuthClient;
import com.developer.sso.dto.GetAccessTokenRequestDTO;
import com.developer.sso.dto.RefreshAccessTokenRequestDTO;
import com.developer.sso.dto.TokenDTO;
import com.developer.sso.dto.VerifyAccessTokenRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TokenServiceImpl implements TokenService {

    @Autowired
    private SnowflakeNoUtil snowflakeNoUtil;

    @Autowired
    private OAuthClient oAuthClient;

    @Override
    public DeveloperResult<TokenDTO> getAccessToken(GetAccessTokenRequestDTO req) {
        String serialNo = snowflakeNoUtil.getSerialNo(req.getSerialNo());
        try {
            Map<String, Object> response = oAuthClient.getToken("password", req.getUserName(), req.getPassword(), "client_dev", "dev");
            TokenDTO tokenDTO = TokenDTO.builder().accessToken(response.get("access_token").toString()).refreshToken(response.get("refresh_token").toString()).build();
            return DeveloperResult.success(serialNo,tokenDTO);
        }catch (FeignException e){
            String errorMsg = parseAndHandleError(e.contentUTF8());
            return DeveloperResult.error(serialNo,e.status(),errorMsg);
        }
    }

    @Override
    public DeveloperResult<TokenDTO> refreshAccessToken(RefreshAccessTokenRequestDTO req) {
        String serialNo = snowflakeNoUtil.getSerialNo(req.getSerialNo());
        try {
            Map<String, Object> response = oAuthClient.refreshToken("dev","refresh_token", req.getRefreshToken(), "client_dev");
            TokenDTO tokenDTO = TokenDTO.builder().accessToken(response.get("access_token").toString()).refreshToken(response.get("refresh_token").toString()).build();
            return DeveloperResult.success(serialNo,tokenDTO);
        }catch (FeignException e){
            String errorMsg = parseAndHandleError(e.contentUTF8());
            return DeveloperResult.error(serialNo,e.status(),errorMsg);
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
