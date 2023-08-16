package com.gduf.mybatis_generator.controller;

import com.gduf.mybatis_generator.Service.CustomerService;
import com.gduf.mybatis_generator.entities.Customer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;

/**
 * @author LuoXuanwei
 * @date 2023/8/16 23:57
 */
@Api(tags = "客户Customer接口+布隆过滤器")
@RestController
@Slf4j
public class CustomerController {
    @Resource
    private CustomerService customerService;

    @ApiOperation("数据库初始化2条Customer记录插入")
    @PostMapping("/customer/add")
    public void addCustomer() {
        for (int i = 0; i < 2; i++) {
            Customer customer = new Customer();
            customer.setCname("customer" + i);
            customer.setAge(new Random().nextInt(30) + 1);
            customer.setPhone("15113900139");
            customer.setSex((byte) new Random().nextInt(2));
            customer.setBirth(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));

            customerService.addCustomer(customer);
        }
    }

    @ApiOperation("单个customer查询操作，按照customerId查询")
    @GetMapping("/customer/{customerId}")
    public Customer findCustomerById(@PathVariable Integer customerId) {
        return customerService.findCustomerById(customerId);
    }

    @ApiOperation("BloomFilter,按照customerId查询")
    @GetMapping("/customerbloomfilter/{customerId}")
    public Customer findCustomerByIdWithBloomFilter(@PathVariable Integer customerId) {
        return customerService.findCustomerByIdWithBloomFilter(customerId);

    }
}
