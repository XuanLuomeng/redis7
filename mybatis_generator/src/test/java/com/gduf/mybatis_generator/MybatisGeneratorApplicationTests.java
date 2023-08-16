package com.gduf.mybatis_generator;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MybatisGeneratorApplicationTests {

    @Test
    void contextLoads() {
    }

    /**
     * 创建guava版布隆过滤器，helloWorld入门级演示
     */
    @Test
    public void testGuavaWithBloomFilter(){
        //1 创建guava版布隆过滤器
        BloomFilter<Integer> bloomFilter = BloomFilter.create(Funnels.integerFunnel(),100);
        //2 判断指定的元素是否存在
        System.out.println(bloomFilter.mightContain(1));
        System.out.println(bloomFilter.mightContain(2));

        System.out.println();
        //3 将元素新增进入bloom filter
        bloomFilter.put(1);
        bloomFilter.put(2);
        System.out.println(bloomFilter.mightContain(1));
        System.out.println(bloomFilter.mightContain(2));
    }

}
