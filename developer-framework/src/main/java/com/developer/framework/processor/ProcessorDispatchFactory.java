package com.developer.framework.processor;

import com.developer.framework.enums.common.ProcessorTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ProcessorDispatchFactory {

    @Autowired
    private ApplicationContext context;

    public IMessageProcessor getInstance(ProcessorTypeEnum processorType){
        Map<String, IMessageProcessor> beansMap = context.getBeansOfType(IMessageProcessor.class);
        for(IMessageProcessor item : beansMap.values()){
            if(item.processorType()==processorType){
                return item;
            }
        }
        return null;
    }

}
