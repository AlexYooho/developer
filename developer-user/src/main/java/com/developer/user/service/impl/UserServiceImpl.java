package com.developer.user.service.impl;

import com.developer.user.dto.UserRegisterDTO;
import com.developer.user.pojo.UserPO;
import com.developer.user.repository.UserRepository;
import com.developer.user.service.UserService;
import com.developer.framework.model.DeveloperResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;


    /**
     * 用户注册
     * @param dto
     * @return
     */
    @Override
    public DeveloperResult register(UserRegisterDTO dto) {
        if("".equals(dto.getAccount())){
            return DeveloperResult.error(500,"请输入正确的手机号");
        }

        if("".equals(dto.getPassword())){
            return DeveloperResult.error(500,"请输入密码");
        }

        if("".equals(dto.getNickname())){
            return DeveloperResult.error(500,"请输入昵称");
        }

        UserPO userPO = userRepository.findByAccount(dto.getAccount());
        if(userPO!=null){
            return DeveloperResult.error(500,"手机号已存在,请重新输入");
        }

        userPO = new UserPO(dto.getAccount(), "", dto.getNickname(), "","", dto.getPassword(), dto.getSex(),0,"",new Date(),new Date());
        userRepository.save(userPO);
        return DeveloperResult.success();
    }


}
