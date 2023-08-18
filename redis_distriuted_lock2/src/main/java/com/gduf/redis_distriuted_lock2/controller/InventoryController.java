package com.gduf.redis_distriuted_lock2.controller;

import com.gduf.redis_distriuted_lock2.service.InventoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author LuoXuanwei
 * @date 2023/8/18 0:16
 */
@RestController
@Api(tags = "redis分布式锁测试")
public class InventoryController {
    @Autowired
    private InventoryService inventoryService;

    @ApiOperation("扣减库存，一次买一个")
    @GetMapping("/inventory/sale")
    public String sale(){
        return inventoryService.sale();
    }
}
