package com.developer.framework.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取任意简单对象
     * @param key
     * @param clazz
     * @return
     * @param <T>
     */
    public <T> T get(String key,Class<T> clazz){
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }

        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        }

        try {
            String jsonValue = value.toString(); // 转换为 JSON 字符串
            return objectMapper.readValue(jsonValue, clazz); // 使用 Jackson 转换为目标类型
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to convert value to type: " + clazz.getName(), e);
        }
    }

    /**
     * 获取任意泛型对象
     * @param key
     * @param typeReference
     * @return
     * @param <T>
     */
    public <T> T get(String key, TypeReference<T> typeReference) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }

        try {
            String jsonValue = value.toString(); // 转换为 JSON 字符串
            return objectMapper.readValue(jsonValue, typeReference); // 使用 Jackson 转换为目标类型
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to convert value to type: " + typeReference.getType(), e);
        }
    }

    /**
     * 存储任意类型的值到 Redis
     *
     * @param key Redis 键
     * @param value 要存储的值
     */
    public void set(String key, Object value,long timeOut, TimeUnit timeUnit) {
        try{
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value),timeOut,timeUnit);
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }
    }

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void increment(String key){
        redisTemplate.opsForValue().increment(key);
    }

    public void setExpire(String key, long timeOut, TimeUnit timeUnit) {
        redisTemplate.expire(key, timeOut, timeUnit);
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
