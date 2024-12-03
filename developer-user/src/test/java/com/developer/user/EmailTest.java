package com.developer.user;

import com.developer.framework.utils.MailUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserApplication.class)
public class EmailTest {

    @Autowired
    private MailUtil mailUtil;

    @Test
    public void Test(){
        Integer code = mailUtil.sendAuthorizationCode();
        System.out.println("验证码："+code);
    }
}
