package com.developer.sso.service;

import com.developer.framework.model.DeveloperResult;
import com.developer.sso.dto.GetAccessTokenRequestDTO;
import com.developer.sso.dto.RefreshAccessTokenRequestDTO;
import com.developer.sso.dto.TokenDTO;
import com.developer.sso.dto.VerifyAccessTokenRequestDTO;

public interface TokenService {

    DeveloperResult<TokenDTO> getAccessToken(GetAccessTokenRequestDTO req);

    DeveloperResult<TokenDTO> refreshAccessToken(RefreshAccessTokenRequestDTO req);

}
