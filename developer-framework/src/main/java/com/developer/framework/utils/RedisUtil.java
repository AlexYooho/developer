package com.developer.framework.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 获取 Redis 中的值并转换为指定类型,针对string
     *
     * @param key Redis 键
     * @param clazz 目标类型的 Class 对象
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        } else if (clazz == String.class) {
            return clazz.cast(value.toString());
        } else if (clazz == Integer.class) {
            return clazz.cast(Integer.parseInt(value.toString()));
        } else if (clazz == Long.class) {
            return clazz.cast(Long.parseLong(value.toString()));
        } else if (clazz == Double.class) {
            return clazz.cast(Double.parseDouble(value.toString()));
        } else if (clazz == Boolean.class) {
            return clazz.cast(Boolean.parseBoolean(value.toString()));
        } else {
            throw new IllegalArgumentException("Unsupported type: " + clazz.getName());
        }
    }

    /**
     * 存储任意类型的值到 Redis
     *
     * @param key Redis 键
     * @param value 要存储的值
     */
    public void set(String key, Object value,long timeOut, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value,timeOut,timeUnit);
    }

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void increment(String key){
        redisTemplate.opsForValue().increment(key);
    }

}
