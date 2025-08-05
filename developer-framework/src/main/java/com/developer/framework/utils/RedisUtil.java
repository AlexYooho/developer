package com.developer.framework.utils;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.*;
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
            return JSON.parseObject(jsonValue,clazz); // 使用 Jackson 转换为目标类型
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
        redisTemplate.opsForValue().set(key, value,timeOut,timeUnit);
    }

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public Long increment(String key,Long incrementCount){
        return redisTemplate.opsForValue().increment(key,incrementCount);
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


    /*----------------------------------------------------------Hash----------------------------------------------------------*/

    /**
     * 设置Hash字段值
     * @param key Hash的键
     * @param field 字段名
     * @param value 字段值
     */
    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * 批量设置Hash字段值
     * @param key Hash的键
     * @param hash 字段-值映射
     */
    public void hmSet(String key, Map<String, Object> hash) {
        redisTemplate.opsForHash().putAll(key, hash);
    }

    /**
     * 获取Hash指定字段的值
     * @param key Hash的键
     * @param field 字段名
     * @return 字段值 or null if not exists
     */
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 批量获取Hash指定字段的值
     * @param key Hash的键
     * @param fields 字段名集合
     * @return 字段值列表
     */
    public List<Object> hmGet(String key, List<String> fields) {
        return redisTemplate.opsForHash().multiGet(key, Collections.singleton(fields));
    }

    /**
     * 获取Hash所有字段和值
     * @param key Hash的键
     * @return 字段-值映射
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 删除Hash指定字段
     * @param key Hash的键
     * @param fields 字段名
     * @return 删除的字段数量
     */
    public long hDel(String key, String... fields) {
        return redisTemplate.opsForHash().delete(key, (Object[]) fields);
    }

    /**
     * 判断Hash字段是否存在
     * @param key Hash的键
     * @param field 字段名
     * @return true if exists
     */
    public boolean hExists(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    /**
     * 获取Hash所有字段名
     * @param key Hash的键
     * @return 字段名集合
     */
    public Set<Object> hKeys(String key) {
        return redisTemplate.opsForHash().keys(key);
    }

    /**
     * 获取Hash所有值
     * @param key Hash的键
     * @return 值集合
     */
    public List<Object> hValues(String key) {
        return redisTemplate.opsForHash().values(key);
    }

    /**
     * 获取Hash字段数量
     * @param key Hash的键
     * @return 字段数量
     */
    public long hLen(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    /**
     * 原子递增Hash字段值
     * @param key Hash的键
     * @param field 字段名
     * @param increment 增量
     * @return 递增后的值
     */
    public long hIncrement(String key, String field, long increment) {
        return redisTemplate.opsForHash().increment(key, field, increment);
    }

    /*----------------------------------------------------------Set----------------------------------------------------------*/

    /**
     * 向Set添加一个或多个元素
     * @param key Set的键
     * @param values 要添加的元素
     * @return 添加的元素数量
     */
    public long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 从Set移除一个或多个元素
     * @param key Set的键
     * @param values 要移除的元素
     * @return 移除的元素数量
     */
    public long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    /**
     * 获取Set所有元素
     * @param key Set的键
     * @return 元素集合
     */
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 判断元素是否在Set中
     * @param key Set的键
     * @param value 元素
     * @return true if exists
     */
    public boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 获取Set的大小
     * @param key Set的键
     * @return 元素数量
     */
    public long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 随机返回并移除Set中的一个元素
     * @param key Set的键
     * @return 移除的元素 or null if Set is empty
     */
    public Object sPop(String key) {
        return redisTemplate.opsForSet().pop(key);
    }

    /**
     * 随机返回Set中的一个元素（不移除）
     * @param key Set的键
     * @return 随机元素 or null if Set is empty
     */
    public Object sRandMember(String key) {
        return redisTemplate.opsForSet().randomMember(key);
    }

    /**
     * 随机返回Set中的多个元素（不移除）
     * @param key Set的键
     * @param count 返回的元素数量
     * @return 随机元素列表
     */
    public List<Object> sRandMember(String key, long count) {
        return redisTemplate.opsForSet().randomMembers(key, count);
    }

    /**
     * 求多个Set的并集
     * @param keys Set的键集合
     * @return 并集结果
     */
    public Set<Object> sUnion(String... keys) {
        return redisTemplate.opsForSet().union(Arrays.asList(keys));
    }

    /**
     * 求多个Set的交集
     * @param keys Set的键集合
     * @return 交集结果
     */
    public Set<Object> sIntersect(String... keys) {
        return redisTemplate.opsForSet().intersect(Arrays.asList(keys));
    }

    /**
     * 求多个Set的差集
     * @param otherKeys Set的键集合
     * @return 差集结果
     */
    public Set<Object> sDiff(String key, String... otherKeys) {
        return redisTemplate.opsForSet().difference(key, Arrays.asList(otherKeys));
    }
}
