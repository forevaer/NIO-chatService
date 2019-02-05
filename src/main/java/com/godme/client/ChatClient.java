package com.godme.client;

import com.godme.codec.MessageCodec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatClient {
    public static void main(String[] args) throws IOException {
        SocketChannel client  = SocketChannel.open();
        client.configureBlocking(false);
        client.connect(new InetSocketAddress("localhost", 8989));
        Selector selector = Selector.open();
        client.register(selector, SelectionKey.OP_CONNECT);
        while(true){
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            selectedKeys.forEach(selectedKey -> {
                try{
                    if(selectedKey.isConnectable()){
                        SocketChannel channel = (SocketChannel) selectedKey.channel();
                        if(channel.isConnectionPending()) {
                            channel.finishConnect();
                        }
                        System.out.println("链接成功");
                        ExecutorService executorService = Executors.newSingleThreadExecutor(Executors.defaultThreadFactory());
                        executorService.submit(()->{
                            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                            while(true){
                                String message = reader.readLine();
                                channel.write(MessageCodec.encode(message));
                            }
                        });
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ);
                    } else if(selectedKey.isReadable()){
                        SocketChannel socketChannel = (SocketChannel) selectedKey.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        socketChannel.read(byteBuffer);
                        byteBuffer.flip();
                        System.out.println(MessageCodec.decode(byteBuffer));
                    }
                }catch (Exception e){
                   System.exit(0);
                }
            });
            selectedKeys.clear();
        }
    }
}
