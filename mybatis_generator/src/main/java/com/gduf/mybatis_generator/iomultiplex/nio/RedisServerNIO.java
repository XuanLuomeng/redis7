package com.gduf.mybatis_generator.iomultiplex.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

/**
 * @author LuoXuanwei
 * @date 2023/8/27 0:15
 */
public class RedisServerNIO {
    static ArrayList<SocketChannel> socketChannelArrayList = new ArrayList<SocketChannel>();
    static ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

    public static void main(String[] args) throws IOException {
        System.out.println("------RedisServerNIO 启动等待中------");
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("127.0.0.1", 6379));
        serverSocket.configureBlocking(false);//设置为非阻塞

        while (true) {
            for (SocketChannel element : socketChannelArrayList) {
                int read = element.read(byteBuffer);
                if (read > 0) {
                    System.out.println("------读取数据------" + read);
                    byteBuffer.flip();
                    byte[] bytes = new byte[read];
                    byteBuffer.get(bytes);
                    System.out.println(new String(bytes));
                    byteBuffer.clear();
                }

                SocketChannel socketChannel = serverSocket.accept();
                if (socketChannel != null) {
                    System.out.println("------连接成功------");
                    socketChannel.configureBlocking(false);
                    socketChannelArrayList.add(socketChannel);
                    System.out.println("------socketList size:------" + socketChannelArrayList.size());
                }
            }
        }
    }
}
