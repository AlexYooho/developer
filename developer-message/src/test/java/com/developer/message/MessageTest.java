package com.developer.message;

import com.developer.framework.enums.MessageMainTypeEnum;
import com.developer.message.service.MessageTypeProcessorDispatchFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MessageApplication.class)
public class MessageTest {

    @Autowired
    private MessageTypeProcessorDispatchFactory messageTypeProcessorDispatchFactory;

    @Test
    public void Test(){
        messageTypeProcessorDispatchFactory.getInstance(MessageMainTypeEnum.PRIVATE_MESSAGE).sendMessage(null);
    }

}
