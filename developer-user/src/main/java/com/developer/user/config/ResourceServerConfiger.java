package com.developer.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;

@Configuration
@EnableResourceServer // 开启资源服务验证
@EnableWebSecurity  // 开启web安全访问
public class ResourceServerConfiger extends ResourceServerConfigurerAdapter {

    /**
     * 远程校验token
     * @param resources
     * @throws Exception
     */
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId("developer_user");
        RemoteTokenServices remoteTokenServices = new RemoteTokenServices();
        remoteTokenServices.setCheckTokenEndpointUrl("http://localhost:9006/oauth/check_token");
        remoteTokenServices.setClientId("client_dev");
        remoteTokenServices.setClientSecret("dev");
        resources.tokenServices(remoteTokenServices);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and().authorizeRequests()
                .antMatchers("/user/index/**").authenticated()
                .anyRequest().permitAll();
    }
}
