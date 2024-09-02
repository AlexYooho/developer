package com.developer.sso.service;

import com.developer.sso.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserService sysUserService;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        UserInfo userInfo = sysUserService.getUserByUserName(userName);
        if (userInfo == null){
            throw new UsernameNotFoundException("用户不存在: " + userName);
        }
        List<GrantedAuthority> authorities=new ArrayList<>();
        //获取用户权限
        List<String> permissions = userInfo.getPermissions();
        permissions.forEach(permission->{
            authorities.add(new SimpleGrantedAuthority("ROLE_" + permission));
        });
        // 这里一定要基于 BCrypt 加密,不然会不通过
        UserDetails user = new User(userInfo.getUsername(), new BCryptPasswordEncoder().encode(userInfo.getPassword()), authorities);
        return user;
    }

}
