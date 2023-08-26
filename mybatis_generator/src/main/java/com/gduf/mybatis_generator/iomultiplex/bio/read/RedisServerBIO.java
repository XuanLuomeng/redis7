package com.gduf.mybatis_generator.iomultiplex.bio.read;

import cn.hutool.core.util.IdUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author LuoXuanwei
 * @date 2023/8/23 0:21
 */
public class RedisServerBIO {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(6379);

        while (true) {
            System.out.println("-----111 等待连接");
            Socket socket = serverSocket.accept();//阻塞1，等待客户端连接
            System.out.println("-----222 连接成功");

            InputStream inputStream = socket.getInputStream();
            int length = -1;
            byte[] bytes = new byte[1024];
            System.out.println("-----333 等待读取");
            while ((length = inputStream.read(bytes)) != -1) {//阻塞2，等待客户端发送数据
                System.out.println("-----444 成功读取" + new String(bytes, 0, length));
                System.out.println("======================\t" + IdUtil.simpleUUID());
                System.out.println("");
            }
        }
    }
}
