package com.zjn.niogroupchat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class ChatServer {

    private Selector selector;
    private ServerSocketChannel listenChannel;

    private final static int PORT = 6666;

    public ChatServer(){

        try {
            // 打开选择器
            selector = Selector.open();
            // 打开通道
            listenChannel = ServerSocketChannel.open();
            // 绑定端口
            listenChannel.socket().bind(new InetSocketAddress(PORT));
            //设置非阻塞模式
            listenChannel.configureBlocking(false);
            // channel 注册到选择器
            listenChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void readData(SelectionKey selectionKey){

        SocketChannel  channel = null;
        try {
            channel = (SocketChannel)selectionKey.channel();

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int read = channel.read(buffer);

            if(read > 0){
                //把缓存区的数据转成字符串
                String msg = new String(buffer.array());
                //输出该消息
                System.out.println("form 客户端: " + msg.trim());

                //向其它的客户端转发消息(去掉自己), 专门写一个方法来处理
                sendInfoToOtherClients(msg, channel);
            }
        } catch (IOException e) {
            try {
                System.out.println(channel.getRemoteAddress() + " 离线了..");
                //取消注册
                selectionKey.cancel();
                //关闭通道
                channel.close();
            }catch (IOException e2) {
                e2.printStackTrace();
            }
        }

    }

    //转发消息给其它客户(通道)
    private void sendInfoToOtherClients(String msg, SocketChannel self ) throws  IOException{

        System.out.println("服务器转发消息中...");
        System.out.println("服务器转发数据给客户端线程: " + Thread.currentThread().getName());
        //遍历 所有注册到selector 上的 SocketChannel,并排除 self
        for(SelectionKey key: selector.keys()) {

            //通过 key  取出对应的 SocketChannel
            Channel targetChannel = key.channel();

            //排除自己
            if(targetChannel instanceof  SocketChannel && targetChannel != self) {

                //转型
                SocketChannel dest = (SocketChannel)targetChannel;
                //将msg 存储到buffer
                ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
                //将buffer 的数据写入 通道
                dest.write(buffer);
            }
        }

    }
    public void listen(){

        System.out.println("开始监听线程：" + Thread.currentThread().getName());
        try {

            while (true){

                int count = selector.select();
                // 有事件处理
                if(count > 0){

                    //得到selectionkey 集合
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                    while (iterator.hasNext()){
                        // 获取 SelectionKey
                        SelectionKey key = iterator.next();
                        //监听到accept 信息
                        if(key.isAcceptable()){
                            // 获取管道接收信息
                            SocketChannel sc = listenChannel.accept();
                            // 设置非阻塞
                            sc.configureBlocking(false);
                            // 将接收信息管道到注册到select
                            sc.register(selector,SelectionKey.OP_READ);
                            //提示
                            System.out.println(sc.getRemoteAddress() + " 上线 ");
                        }
                        // 向通道发送读事件，通道可读
                        if(key.isReadable()){

                            readData(key);
                        }
                        //当前的key 删除，防止重复处理
                        iterator.remove();

                    }

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        //创建服务器对象
        ChatServer groupChatServer = new ChatServer();
        groupChatServer.listen();
    }
}
