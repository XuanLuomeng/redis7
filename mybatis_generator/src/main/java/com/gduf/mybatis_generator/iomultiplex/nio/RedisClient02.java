package com.gduf.mybatis_generator.iomultiplex.nio;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @author LuoXuanwei
 * @date 2023/8/27 0:15
 */
public class RedisClient02 {
    public static void main(String[] args) throws IOException {
        System.out.println("------RedisClient02 start");
        Socket socket = new Socket("127.0.0.1", 6379);
        OutputStream outputStream = socket.getOutputStream();
        while (true) {
            Scanner scanner = new Scanner(System.in);
            String string = scanner.next();
            if (string.equalsIgnoreCase("quit")) {
                break;
            }
            socket.getOutputStream().write(string.getBytes(StandardCharsets.UTF_8));
            System.out.println("------input quit keyword to finish------");
        }
        outputStream.close();
        socket.close();
    }
}
