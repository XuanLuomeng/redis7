package com.gduf.mybatis_generator.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author LuoXuanwei
 * @date 2023/8/17 0:30
 */
@Component
@Slf4j
public class CheckUtils {
    @Resource
    private RedisTemplate redisTemplate;

    public boolean checkWithBloomFilter(String checkItem, String key) {
        int hashValue = Math.abs(key.hashCode());
        long index = (long) (hashValue % Math.pow(2, 32));
        boolean existOK = redisTemplate.opsForValue().getBit(checkItem, index);
        log.info("--->key:" + key + "对应坑位下标index:" + index + "是否存在:" + existOK);
        return existOK;
    }
}
