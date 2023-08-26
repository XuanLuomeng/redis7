package com.gduf.mybatis_generator.iomultiplex.bio.accept;

import java.io.IOException;
import java.net.Socket;

/**
 * @author LuoXuanwei
 * @date 2023/8/23 0:16
 */
public class RedisClient01 {
    public static void main(String[] args) throws IOException {
        System.out.println("------RedisClient01 start");
        Socket socket = new Socket("127.0.0.1",6379);
        System.out.println("------RedisClient01 connection over");
    }
}
