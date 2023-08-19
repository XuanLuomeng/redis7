package com.gduf.red_lock.config;

import lombok.Data;

/**
 * @author LuoXuanwei
 * @date 2023/8/18 23:50
 */
@Data
public class RedisPoolProperties {

    private int maxIdle;

    private int minIdle;

    private int maxActive;

    private int maxWait;

    private int connTimeout;

    private int soTimeout;

    /**
     * 池大小
     */
    private  int size;

}
