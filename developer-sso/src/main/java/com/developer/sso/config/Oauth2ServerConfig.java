package com.developer.sso.config;

import com.developer.sso.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;


@Configuration
@EnableAuthorizationServer
public class Oauth2ServerConfig extends AuthorizationServerConfigurerAdapter {

    /**
     * 认证管理器
     */
    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * 密码加密方式
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 自定义身份认证
     */
    @Autowired
    private UserServiceImpl userDetailsService;

    /**
     * token存储方式
     * @return
     */
    public TokenStore tokenStore(){
        return new JwtTokenStore(jwtAccessTokenConverter());
    }


    private String SIGN_KEY="developer";

    @Autowired
    private AccessTokenConvertor accessTokenConvertor;

    public JwtAccessTokenConverter jwtAccessTokenConverter(){
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        jwtAccessTokenConverter.setSigningKey(SIGN_KEY);
        jwtAccessTokenConverter.setVerifier(new MacSigner(SIGN_KEY));
        jwtAccessTokenConverter.setAccessTokenConverter(accessTokenConvertor);

        return jwtAccessTokenConverter;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        super.configure(clients);
        //配置 client信息从数据库中取
        clients.inMemory()
                .withClient("client_dev")
                .secret("dev")
                .resourceIds("developer_friend","developer_group","developer_im","developer_message","developer_oss","developer_sso","developer_user")
                .authorizedGrantTypes("password","refresh_token")
                .scopes("all");
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        super.configure(endpoints);
        endpoints
                .tokenStore(tokenStore())//token存储方式
                .authenticationManager(authenticationManager)// 开启密码验证，由 WebSecurityConfigurerAdapter
                .userDetailsService(userDetailsService)// 读取验证用户信息
                .allowedTokenEndpointRequestMethods(HttpMethod.GET,HttpMethod.POST,HttpMethod.DELETE,HttpMethod.PUT)
                .tokenServices(authorizationServerTokenServices());
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        super.configure(security);
        security.tokenKeyAccess("permitAll()")
                .checkTokenAccess("isAuthenticated()")
                .allowFormAuthenticationForClients()
                .passwordEncoder(NoOpPasswordEncoder.getInstance());
    }

    /**
     * token相关信息
     * @return
     */
    public AuthorizationServerTokenServices authorizationServerTokenServices(){
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setSupportRefreshToken(true);
        defaultTokenServices.setTokenStore(tokenStore());
        defaultTokenServices.setAccessTokenValiditySeconds(60*60*2);
        defaultTokenServices.setRefreshTokenValiditySeconds(60*60*24);
        defaultTokenServices.setTokenEnhancer(jwtAccessTokenConverter());
        return defaultTokenServices;
    }
}
