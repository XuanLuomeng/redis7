package com.gduf.mybatis_generator.controller;

import com.gduf.mybatis_generator.entities.Product;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author LuoXuanwei
 * @date 2023/8/17 18:21
 */
@RestController
@Slf4j
@Api(tags = "聚划算商品列表接口")
public class JHSProductController {
    public static final String JHS_KEY = "jhs";
    public static final String JHS_KEY_A = "jhs:a";
    public static final String JHS_KEY_B = "jhs:b";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 分页查询:在高并发的情况下，只能走redis查询，走db的话会把db打垮
     *
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/product/find")
    @ApiOperation("聚划算案例，每次1页每页显示5条")
    public List<Product> find(int page, int size) {
        List<Product> list = null;

        long start = (page - 1) * size;
        long end = start + size - 1;

        try {
            // 采用redis list结构里面的lrang命令来实现加载和分页查询
            list = redisTemplate.opsForList().range(JHS_KEY, start, end);
            if (CollectionUtils.isEmpty(list)) {
                //TODO 走mysql查询
            }
            log.info("参加活动的商家:{}", list);
        } catch (Exception e) {
            // 出现异常，一般redis宕机了或者redis网络抖动导致timeout
            log.error("jhs exception:{}", e);
            e.printStackTrace();
            // ...再次查询mysql
        }
        return list;
    }

    @GetMapping("/product/findab")
    @ApiOperation("AB双缓存架构，防止热点key突然失效")
    public List<Product> findAB(int page, int size) {
        List<Product> list = null;
        long start = (page - 1) * size;
        long end = start + size - 1;
        try {
            list = redisTemplate.opsForList().range(JHS_KEY_A, start, end);
            if (CollectionUtils.isEmpty(list)) {
                log.info("---A缓存已经过期失效或者活动结束了，记得人工修改，B缓存继续顶着");
                list = redisTemplate.opsForList().range(JHS_KEY_B, start, end);
                if (CollectionUtils.isEmpty(list)) {
                    //TODO 走mysql查询
                }
            }
        } catch (Exception e) {
            // 出现异常，一般redis宕机了或者redis网络抖动导致timeout
            log.error("jhs exception:{}", e);
            e.printStackTrace();
            // ...再次查询mysql
        }
        return list;
    }
}
