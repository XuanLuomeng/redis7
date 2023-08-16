package com.gduf.mybatis_generator.controller;

import com.gduf.mybatis_generator.Service.GuavaBloomFilterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author LuoXuanwei
 * @date 2023/8/17 1:51
 */
@Api(tags = "google工具Guava处理布隆过滤器")
@RestController
@Slf4j
public class GuavaBloomFilterController {
    @Resource
    private GuavaBloomFilterService guavaBloomFilterService;

    @ApiOperation("guava布隆过滤器插入100万样本数据并额外10w测试是否岑在")
    @GetMapping("/guavafilter")
    public void guavaBloomFilter(){
        guavaBloomFilterService.guavaBloomFilter();
    }
}
