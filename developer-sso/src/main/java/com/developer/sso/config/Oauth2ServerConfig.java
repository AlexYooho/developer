package com.developer.sso.config;

import com.developer.sso.service.UserServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

@AllArgsConstructor
@Configuration
@EnableAuthorizationServer
public class Oauth2ServerConfig extends AuthorizationServerConfigurerAdapter {
    // 认证管理器
    @Autowired
    private AuthenticationManager authenticationManager;

    //密码加密方式
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 自定义身份认证
    @Autowired
    private UserServiceImpl userDetailsService;

    @Bean
    public TokenStore tokenStore(){
        // token存储
        return new InMemoryTokenStore();
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        super.configure(clients);
        //配置 client信息从数据库中取
        clients.inMemory()
                .withClient("client_dev")
                .secret("dev")
                .resourceIds("developer_friend","developer_group","developer_im","developer_message","developer_oss","developer_user")
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
        //  配置Endpoint,允许请求
        security.tokenKeyAccess("permitAll()") // 开启/oauth/token_key 验证端口-无权限
                .checkTokenAccess("permitAll()") // 开启/oauth/check_token 验证端口-需权限
                .allowFormAuthenticationForClients()// 允许表单认证
                .passwordEncoder(NoOpPasswordEncoder.getInstance());   // 配置BCrypt加密
    }

    /**
     * token相关信息
     * @return
     */
    public AuthorizationServerTokenServices authorizationServerTokenServices(){
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setSupportRefreshToken(true);
        defaultTokenServices.setTokenStore(tokenStore());
        defaultTokenServices.setAccessTokenValiditySeconds(120);
        defaultTokenServices.setRefreshTokenValiditySeconds(180);
        return defaultTokenServices;
    }
}
