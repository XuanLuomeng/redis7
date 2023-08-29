package com.gduf.mybatis_generator.controller;

import cn.hutool.core.util.IdUtil;
import com.google.common.primitives.Ints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author LuoXuanwei
 * @date 2023/8/28 18:35
 */
@RestController
public class RedPackeageController {
    public static final String RED_PACKAGE_KEY = "redpackage:";
    public static final String RED_PACKAGE_CONSUME_KEY = "redpackage:consume:";

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping("/send")
    public String sendRedPackage(int totalMoney, int redPackageNumber) {
        //1 拆红包，将总金额totalMoney拆分为redPackageNumber个子红包
        Integer[] splitRedPackages = splitRedPackageAlgorithm(totalMoney, redPackageNumber);//拆分红包算法通过后获得的多个子红包list数组

        //2 发红包并保存进list结构里并设置过期时间
        String key = RED_PACKAGE_KEY + IdUtil.simpleUUID();
        redisTemplate.opsForList().leftPushAll(key, splitRedPackages);
        redisTemplate.expire(key, 1, TimeUnit.DAYS);

        //3 发红包OK，返回前台显示
        return key + "\t" + Ints.asList(Arrays.stream(splitRedPackages).mapToInt(Integer::valueOf).toArray());
    }

    @RequestMapping("/rob")
    public String robRedPackage(String redPackageKey, String userId) {
        //1 验证某个用户是否抢过红包，不可以多抢
        Object redPackage = redisTemplate.opsForHash().get(RED_PACKAGE_CONSUME_KEY + redPackageKey, userId);
        //2 没有抢过可以去抢红包，否则返回-2表示该用户抢过红包了
        if (null == redPackage) {
            //2.1 从大红包（list）里面出队一个作为该客户的红包，抢到了一个红包
            Object partRedPackage = redisTemplate.opsForList().leftPop(RED_PACKAGE_KEY + redPackageKey);
            if (partRedPackage != null) {
                //2.2 抢到红包后需要记录进入hash结构，表示谁抢到了多少钱的某个子红包
                redisTemplate.opsForHash().put(RED_PACKAGE_CONSUME_KEY + redPackageKey, userId, partRedPackage);
                System.out.println("用户:" + userId + "\t抢到了多少钱的红包:" + partRedPackage);
                //TODO 后续异步进mysql或MQ进一步做统计处理，每一年发出多少红包，抢到多少红包，年度总结
                return String.valueOf(partRedPackage);
            }
            //抢完了
            return "errorCode:-1;红包抢完了!";
        }
        //3 某个用户抢过了，不可以再次抢红包
        return "errorCodeL-2" + "\t" + "message:" + userId + "\t " + " 你已经抢过红包了，不可重新抢 !";
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
