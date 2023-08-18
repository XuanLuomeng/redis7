package com.gduf.mybatis_generator.Service;

import cn.hutool.core.date.DateUtil;
import com.gduf.mybatis_generator.entities.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author LuoXuanwei
 * @date 2023/8/17 18:10
 */
@Service
@Slf4j
public class JHSTaskService {
    public static final String JHS_KEY = "jhs";
    public static final String JHS_KEY_A = "jhs:a";
    public static final String JHS_KEY_B = "jhs:b";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 以下为模拟，暂不加入mybatis功能
     *
     * @return
     */
    private List<Product> getProductsFromMysql() {
        List<Product> list = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            Random rand = new Random();
            int id = rand.nextInt(10000);
            Product obj = new Product((long) id, "product" + i, i, "detail");
            list.add(obj);
        }
        return list;
    }

    //@PostConstruct
    public void initJHS() {
        log.info("启动定时器天猫聚划算功能模拟开始————");

        //1 用线程模拟定时任务，后台任务定时将mysql里面的参加活动的商品刷新到redis里
        new Thread(() -> {
            while (true) {
                //2 模拟从mysql查出数据，用于加载到redis并给聚划算页面显示
                List<Product> list = this.getProductsFromMysql();
                //3 采用redis list数据结构的lpush命令来实现存储
                redisTemplate.delete(JHS_KEY);
                //4 加入最新的数据给redis参加活动
                redisTemplate.opsForList().leftPushAll(JHS_KEY, list);
                //5 暂停1分钟，间隔一分钟执行一次，模拟聚划算一天执行的参加活动的品牌
                try {
                    synchronized (this) {
                        TimeUnit.MINUTES.sleep(1);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "t1").start();
    }

    @PostConstruct
    public void initJHSAB(){
        log.info("启动AB定时器计划任务淘宝聚划算功能模拟......"+ DateUtil.now());

        //1 用线程模拟定时任务，后台任务定时将mysql里面的参加活动的商品刷新到redis里
        new Thread(() -> {
            while (true) {
                //2 模拟从mysql查出数据，用于加载到redis并给聚划算页面显示
                List<Product> list = this.getProductsFromMysql();
                //3 先更新B缓存且让B缓存过期时间超过A缓存，如果A突然失效了还有B兜底，防止击穿
                redisTemplate.delete(JHS_KEY_B);
                redisTemplate.opsForList().leftPushAll(JHS_KEY_B,list);
                redisTemplate.expire(JHS_KEY_B,86410L,TimeUnit.SECONDS);
                //4 再更新A缓存
                redisTemplate.delete(JHS_KEY_A);
                redisTemplate.opsForList().leftPushAll(JHS_KEY_A,list);
                redisTemplate.expire(JHS_KEY_A,86400L,TimeUnit.SECONDS);
                //5 暂停1分钟，间隔一分钟执行一次，模拟聚划算一天执行的参加活动的品牌
                try {
                    //防止线程被重复启动
                    synchronized (this) {
                        TimeUnit.MINUTES.sleep(1);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "t1").start();
    }
}
