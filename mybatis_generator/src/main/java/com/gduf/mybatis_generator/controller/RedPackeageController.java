package com.gduf.mybatis_generator.controller;

import cn.hutool.core.util.IdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

/**
 * @author LuoXuanwei
 * @date 2023/8/28 18:35
 */
@RestController
public class RedPackeageController {
    public static final String RED_PACKAGE_KEY = "redpackage:";
    public static final String RED_PACKAGE_CONSUME_KEY = "redpackage:consume";

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping("/send")
    public String sendRedPackage(int totalMoney, int redpackageNumber) {
        //1 拆红包，将总金额totalMoney拆分为redPackageNumber个子红包
        Integer[] splitRedPackages = splitRedPackageAlgorithm(totalMoney, redpackageNumber);//拆分红包算法通过后获得的多个子红包list数组

        //2 发红包并保存进list结构里并设置过期时间
        String key = RED_PACKAGE_KEY + IdUtil.simpleUUID();
        redisTemplate.opsForList().leftPushAll(key, splitRedPackages);


        return null;
    }

    /**
     * 3 拆红包的算法--->二倍均值法
     *
     * @param totalMoney
     * @param redpackageNumber
     * @return
     */
    private Integer[] splitRedPackageAlgorithm(int totalMoney, int redpackageNumber) {
        Integer[] redPackageNumbers = new Integer[redpackageNumber];
        //已经被抢夺的红包金额,已经被拆分塞进子红包的金额
        int useMoney = 0;

        for (int i = 0; i < redpackageNumber; i++) {
            if (i == redpackageNumber - 1) {
                redPackageNumbers[i] = totalMoney - useMoney;
            } else {
                //二倍均值算法，每次拆分后塞进子红包的金额 = 随机区间(0,(剩余红包金额M÷未被抢的剩余红包个数N)*2)
                int avgMoney = ((totalMoney - useMoney) / (redpackageNumber - i)) * 2;
                redPackageNumbers[i] = 1 + new Random().nextInt(avgMoney - 1);
            }
            useMoney = useMoney + redPackageNumbers[i];
        }
        return redPackageNumbers;
    }
}
