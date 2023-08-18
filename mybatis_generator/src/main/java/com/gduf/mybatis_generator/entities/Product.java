package com.gduf.mybatis_generator.entities;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LuoXuanwei
 * @date 2023/8/17 18:07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "聚划算互动product信息")
public class Product {
    //产品ID
    private Long id;
    //产品名称
    private String name;
    //产品价格
    private Integer price;
    //产品详细
    private String detail;
}
