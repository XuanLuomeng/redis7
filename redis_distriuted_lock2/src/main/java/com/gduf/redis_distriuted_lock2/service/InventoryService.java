package com.gduf.redis_distriuted_lock2.service;

import cn.hutool.core.util.IdUtil;
import com.gduf.redis_distriuted_lock2.myLock.DistributedLockFactory;
import com.gduf.redis_distriuted_lock2.myLock.RedisDistributedLock;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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
    @Autowired
    private DistributedLockFactory distributedLockFactory;

    /**
     * v9.0，引入redisson对应的官网推荐RedLock算法实现类
     *
     * @return
     */
    @Autowired
    private Redisson redisson;

    public String saleByRedisson() {
        String retMessage = "";

        RLock redissonLock = redisson.getLock("XXServiceRedisLock");
        redissonLock.lock();
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
                //暂停120秒演示自动续期
                try {
                    TimeUnit.SECONDS.sleep(120);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                retMessage = "商品卖完了";
            }
        } finally {
            //改进点，只能删除自己的锁，不可以删除别人的锁！
            if (redissonLock.isLocked() && redissonLock.isHeldByCurrentThread()) {
                redissonLock.unlock();
            }
        }
        return retMessage + "\t" + "服务端口号:" + port;
    }

    /**
     * v8.0
     * 实现自动续期功能完善，后台自定义扫描程序，如果规定时间内没有完成业务逻辑，会调用加钟自动续期的脚本
     *
     * @return
     */
    public String sale() {
        String retMessage = "";
        Lock redisLock = distributedLockFactory.getDistributedLock("redis");
        redisLock.lock();
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
                //暂停120秒演示自动续期
                try {
                    TimeUnit.SECONDS.sleep(120);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                retMessage = "商品卖完了";
            }
        } finally {
            redisLock.unlock();
        }
        return retMessage + "\t" + "服务端口号:" + port;
    }

    /**
     * v7.0
     * 将lock/unlock+lua脚本自研版分布式锁搞定
     * @return
     */
    /*public String sale() {
        String retMessage = "";
        Lock redisLock = distributedLockFactory.getDistributedLock("redis");
        redisLock.lock();
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
            redisLock.unlock();
        }
        return retMessage + "\t" + "服务端口号:" + port;
    }*/

    /**
     * v6.0(小公司可以使用以下代码，大公司要加强锁的可重入性)
     * @return
     */
    /*public String sale() {
        String retMessage = "";
        String key = "zzyyRedisLock";
        String uuidValue = IdUtil.simpleUUID() + ":" + Thread.currentThread().getId();

        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, uuidValue);
        //不用递归了，高并发下容易出错，我们用自旋替代递归方法重试调用；也不用if，用while替代
        while (!stringRedisTemplate.opsForValue().setIfAbsent(key,uuidValue,30L,TimeUnit.SECONDS)){
            //暂停20毫秒，进行递归重试
            try {
                TimeUnit.MILLISECONDS.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //stringRedisTemplate.expire(key,30L,TimeUnit.SECONDS);

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
            //改进点，修改为Lua脚本的redis分布式锁调用必须保证原子性，参考官网脚本案例
            String luaScript =
                    "if redis.call('get',KEYS[1] == ARGV[1] then " +
                            "return redis.call('del',KEYS[1]) " +
                            "else " +
                            "return 0 " +
                            "end)";
            stringRedisTemplate.execute(new DefaultRedisScript(luaScript,Long.class), Arrays.asList(key),uuidValue);
        }

        return retMessage + "\t" + "服务端口号:" + port;
    }*/

    /**
     * v5.0
     * 最后判断+del非原子性操作，需要用lua脚本进行完成
     * @return
     */
    /*public String sale() {
        String retMessage = "";
        String key = "zzyyRedisLock";
        String uuidValue = IdUtil.simpleUUID() + ":" + Thread.currentThread().getId();

        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, uuidValue);
        //不用递归了，高并发下容易出错，我们用自旋替代递归方法重试调用；也不用if，用while替代
        while (!stringRedisTemplate.opsForValue().setIfAbsent(key,uuidValue,30L,TimeUnit.SECONDS)){
            //暂停20毫秒，进行递归重试
            try {
                TimeUnit.MILLISECONDS.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //stringRedisTemplate.expire(key,30L,TimeUnit.SECONDS);

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
            //改进点，只能删除属于自己的key，不可以删除别人的key
            //判断加锁与解锁是否是同一个客户端，防止误删
            if (stringRedisTemplate.opsForValue().get(key).equalsIgnoreCase(uuidValue)){
                stringRedisTemplate.delete(key);
            }
        }

        return retMessage + "\t" + "服务端口号:" + port;
    }*/

    /**
     * v4.0
     * 第一个线程实际业务时间超过了锁的过期时间，导致第二个线程建锁后
     * 第一个线程业务完成后将第二个线程建的锁给误删了
     * 删除锁的时候需要判断锁是否是自己的
     * @return
     */
    /*public String sale() {
        String retMessage = "";
        String key = "zzyyRedisLock";
        String uuidValue = IdUtil.simpleUUID() + ":" + Thread.currentThread().getId();

        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, uuidValue);
        //不用递归了，高并发下容易出错，我们用自旋替代递归方法重试调用；也不用if，用while替代
        while (!stringRedisTemplate.opsForValue().setIfAbsent(key,uuidValue,30L,TimeUnit.SECONDS)){
            //暂停20毫秒，进行递归重试
            try {
                TimeUnit.MILLISECONDS.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //stringRedisTemplate.expire(key,30L,TimeUnit.SECONDS);

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
    }*/

    /**
     * v3.2
     * 部署了微服务的java程序机器宕机了，代码层面根本没有走到finally这块，
     * 没办法保证解锁（无过期时间该key一直存在），这个key没有被删除，需要加入一个过期时间限定key
     * @return
     */
    /*public String sale() {
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
    }*/

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
