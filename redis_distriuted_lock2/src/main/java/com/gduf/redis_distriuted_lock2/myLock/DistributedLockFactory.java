package com.gduf.redis_distriuted_lock2.myLock;

import cn.hutool.core.util.IdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;

/**
 * @author LuoXuanwei
 * @date 2023/8/18 16:18
 */
@Component
public class DistributedLockFactory {
    private String lockName;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private String uuid;

    public DistributedLockFactory() {
        this.uuid = IdUtil.simpleUUID();
    }

    public Lock getDistributedLock(String lockType) {
        if (lockType == null) {
            return null;
        }
        if (lockType.equalsIgnoreCase("REDIS")) {
            this.lockName = "XXServiceRedisLock";
            return new RedisDistributedLock(stringRedisTemplate, lockName,uuid);
        }else if (lockType.equalsIgnoreCase("ZOOKEEPER")){
            this.lockName = "XXServiceZookeeperLock";
            //TODO zookeeper版本的分布式锁
            return null;
        }else if (lockType.equalsIgnoreCase("MYSQL")){
            this.lockName = "XXServiceMysqlLock";
            //TODO mysql版本的分布式锁
            return null;
        }
        return null;
    }
}
