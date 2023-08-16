package com.gduf.mybatis_generator.Filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author LuoXuanwei
 * @date 2023/8/17 0:20
 */
@Component
@Slf4j
public class BloomFilterInit {
    @Resource
    private RedisTemplate redisTemplate;

    @PostConstruct//初始化白名单数据
    public void init() {
        //1 白名单客户加载到布隆过滤器
        String key = "customer:12";
        //2 计算hashValue，由于存在计算出来负数的可能，我们取绝对值
        int hashValue = Math.abs(key.hashCode());
        //3 通过hashValue和2的32次方后取余，获得对应的下标坑位
        long index = (long) (hashValue % Math.pow(2, 32));
        log.info(key + "对应的坑位index:{}", index);
        //4 设置redis里面的bitmap对应类型的坑位，将该值设置为1
        redisTemplate.opsForValue().setBit("whitelistCustomer",index,true);
    }
}
