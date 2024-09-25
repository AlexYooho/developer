package com.developer.user.config;

import com.developer.user.converter.AccessTokenConvertor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
@EnableResourceServer // 开启资源服务验证
@EnableWebSecurity  // 开启web安全访问
public class ResourceServerConfiger extends ResourceServerConfigurerAdapter {

    private String SIGN_KEY="developer";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 远程校验token
     * @param resources
     */
    @Override
    public void configure(ResourceServerSecurityConfigurer resources)  {
        resources.resourceId("developer_user").tokenStore(tokenStore()).stateless(true);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and().authorizeRequests()
                .antMatchers("/**").authenticated();
    }

    public TokenStore tokenStore(){
        // token存储
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    @Autowired
    private AccessTokenConvertor accessTokenConvertor;

    public JwtAccessTokenConverter jwtAccessTokenConverter(){
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        jwtAccessTokenConverter.setSigningKey(SIGN_KEY);
        jwtAccessTokenConverter.setVerifier(new MacSigner(SIGN_KEY));
        jwtAccessTokenConverter.setAccessTokenConverter(accessTokenConvertor);
        return jwtAccessTokenConverter;
    }
}
