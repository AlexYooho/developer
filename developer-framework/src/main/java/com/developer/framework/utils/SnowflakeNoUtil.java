package com.developer.framework.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RefreshScope
public class SnowflakeNoUtil {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private static final long MAX_SEQUENCE = 999999999999L; // 12 位序列号最大值

    @Value("${machineId}")
    private Integer machineId;

    @Autowired
    private RedisUtil redisUtil;

    public String getSerialNo(){
        // 获取当前毫秒级时间戳
        String timestamp = LocalDateTime.now().format(formatter);

        // 从 Redis 获取当前毫秒的序列号并自增
        String redisKey = "snowflake:sequence:" + timestamp;
        Long sequence = redisUtil.increment(redisKey, 1L);
        if (sequence == null || sequence > MAX_SEQUENCE) {
            // 序列号溢出，等待下一毫秒
            while (timestamp.equals(LocalDateTime.now().format(formatter))) {
                Thread.yield();
            }
            return getSerialNo(); // 递归调用
        }
        // 设置过期时间，避免 Redis 键过多
        redisUtil.setExpire(redisKey, 10, java.util.concurrent.TimeUnit.SECONDS);

        // 格式化机器 ID 和序列号
        String machineIdStr = String.format("%03d", machineId);
        String sequenceStr = String.format("%012d", sequence);

        return timestamp + machineIdStr + sequenceStr;
    }

    public String getSerialNo(String serialNo){
        return !StringUtils.hasText(serialNo) ? getSerialNo() : serialNo;
    }
}
