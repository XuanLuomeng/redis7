package com.gduf.redis_distriuted_lock3.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author LuoXuanwei
 * @date 2023/8/18 0:16
 */
@Service
@Slf4j
public class InventoryService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Value(("${server.port}"))
    private String port;

    private Lock lock = new ReentrantLock();

    public String sale() {
        String retMessage = "";
        lock.lock();
        try {
            //1 查询库存信息
            String result = stringRedisTemplate.opsForValue().get("inventory001");
            //2 判断库存是否足够
            Integer inventoryNumber = result == null ? 0 : Integer.parseInt(result);
            //3 扣减库存，每次减少一个
            if (inventoryNumber > 0) {
                stringRedisTemplate.opsForValue().set("inventory001", String.valueOf(--inventoryNumber));
                retMessage = "成功卖出一个商品，库存剩余:" + inventoryNumber;
                System.out.println(retMessage + "\t" + "服务端口号:" + port);
            } else {
                retMessage = "商品卖完了";
            }
        } finally {
            lock.unlock();
        }
        return retMessage + "\t" + "服务端口号:" + port;
    }
}
