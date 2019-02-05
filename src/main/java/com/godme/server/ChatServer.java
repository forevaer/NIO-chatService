package com.godme.server;

import com.godme.codec.MessageCodec;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

public class ChatServer {
    public static HashSet< SocketChannel> users = new HashSet<>();
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress("localhost",8989));

        Selector selector = Selector.open();
        serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);
        while(true){
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            selectionKeys.forEach(selectionKey -> {
                try{
                    if(selectionKey.isAcceptable()){
                        ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
                        SocketChannel client = server.accept();
                        Integer username = getUsername(client)
;                        String message = "【"+username+"】登录";
                        System.out.println(message);
                        boardCase(message);
                        users.add( client);
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                    }else if(selectionKey.isReadable()){
                        SocketChannel client = (SocketChannel) selectionKey.channel();
                       try{
                           ByteBuffer buffer = ByteBuffer.allocate(1024);
                           client.read(buffer);
                           buffer.flip();
                           String message = "【"+getUsername(client)+"】:" + MessageCodec.decode(buffer) ;
                           System.out.println(message);
                           boardCase(message);
                       }catch (Exception e){
                           selectionKey.cancel();
                           users.remove(client);
                           String message = "【"+getUsername(client)+"】: 退出";
                           System.out.println(message);
                           boardCase(message);
                           client.close();
                       }
                    }
                }catch (Exception e){
                   e.printStackTrace();
                }
            });
            selectionKeys.clear();
        }


    }
    public static Integer getUsername(SocketChannel channel) throws IOException {
        InetSocketAddress address = (InetSocketAddress) channel.getRemoteAddress();
        return address.getPort();
    }
    public static void boardCase(String message){
        if(!users.isEmpty()){
            users.forEach(socketChannel -> {
                try {
                    socketChannel.write(MessageCodec.encode(message));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
