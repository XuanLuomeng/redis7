package com.gduf.redis_distriuted_lock2.service;

import cn.hutool.core.util.IdUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
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

    public String sale() {
        String retMessage = "";
        String key = "zzyyRedisLock";
        String uuidValue = IdUtil.simpleUUID() + ":" + Thread.currentThread().getId();

        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, uuidValue);
        //不用递归了，高并发下容易出错，我们用自旋替代递归方法重试调用；也不用if，用while替代
        while (!stringRedisTemplate.opsForValue().setIfAbsent(key,uuidValue)){
            //暂停20毫秒，进行递归重试
            try {
                TimeUnit.MILLISECONDS.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //抢锁成功的请求线程，进行正常的业务逻辑操作，扣减库存
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
            stringRedisTemplate.delete(key);
        }

        return retMessage + "\t" + "服务端口号:" + port;
    }

    /*
    递归重试，容易导致stackoverflowerror所以不太推荐
    另外，高并发唤醒后推荐用while而不是if
    public String sale() {
        String retMessage = "";
        String key = "zzyyRedisLock";
        String uuidValue = IdUtil.simpleUUID() + ":" + Thread.currentThread().getId();

        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, uuidValue);
        //抢不到线程要继续重试。。。
        if (!flag) {
            //暂停20毫秒，进行递归重试
            try {
                TimeUnit.MILLISECONDS.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            //抢锁成功的请求线程，进行正常的业务逻辑操作，扣减库存
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
                stringRedisTemplate.delete(key);
            }
        }

        return retMessage + "\t" + "服务端口号:" + port;
    }*/

    /*private Lock lock = new ReentrantLock();

    高并发分布式锁的性能需求无法满足
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
    }*/
}
