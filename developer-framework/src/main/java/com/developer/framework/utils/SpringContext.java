package com.developer.framework.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContext implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    /**
     * 设置上下文
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContext.applicationContext=applicationContext;
    }

    /**
     * 获取spring上下文
     * @return
     */
    public static ApplicationContext getApplicationContext(){
        assertApplicationContext();
        return applicationContext;
    }

    /**
     * 获取bean
     * @param beanName
     * @return
     * @param <T>
     */
    public static <T> T getBean(String beanName){
        assertApplicationContext();
        return (T) applicationContext.getBean(beanName);
    }

    /**
     * 获取bean
     * @param requiredType
     * @return
     * @param <T>
     */
    public static <T> T getBean(Class<T> requiredType) {
        assertApplicationContext();
        return applicationContext.getBean(requiredType);
    }

    /**
     * 上下文是否为空
     */
    private static void assertApplicationContext(){
        if(SpringContext.applicationContext==null){
            throw new RuntimeException("sping上下文是否注入!");
        }
    }

}
