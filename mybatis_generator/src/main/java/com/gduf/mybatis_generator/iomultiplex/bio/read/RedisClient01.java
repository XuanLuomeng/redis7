package com.gduf.mybatis_generator.iomultiplex.bio.read;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author LuoXuanwei
 * @date 2023/8/23 0:21
 */
public class RedisClient01 {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("127.0.0.1", 6397);
        OutputStream outputStream = socket.getOutputStream();

        while (true) {
            Scanner scanner = new Scanner(System.in);
            String string = scanner.next();
            if (string.equalsIgnoreCase("quit")) {
                break;
            }
            socket.getOutputStream().write(string.getBytes());
            System.out.println("------RedisClient01 input quit keyword to finish------");
        }
        outputStream.close();
        socket.close();
    }
}
