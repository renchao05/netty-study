package com.renchao;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * @author ren_chao
 * @version 2024-11-01
 */
public class Test03_Read {
    static NioEventLoop eventLoop = (NioEventLoop) new NioEventLoopGroup().next();

    public static void main(String[] args) {
        NioServerSocketChannel serverSocketChannel = new NioServerSocketChannel();
        // 添加当有连接时的处理逻辑
        serverSocketChannel.pipeline().addLast(new MyAcceptor());
        // 注册到EventLoop
        eventLoop.register(serverSocketChannel);
        // 绑定端口
        serverSocketChannel.bind(new InetSocketAddress(8088));

        System.out.println("主线程结束。。。。");
    }

    /**
     * 处理新连接
     */
    static class MyAcceptor extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("有新客户端连接。。。。。。");
            Channel channel = (Channel) msg;
            // 向管道pipeline中添加处理器
            channel.pipeline().addLast(new MyHandler());
            // 将channel注册到EventLoop
            eventLoop.register(channel);
        }
    }


    /**
     * 每个 Channel 对应一个 Pipeline
     * 每个 Handler 对应一个 ChannelHandlerContext（双向链表，封装的 Handler）
     * Pipeline 初始是一个 head 和 一个 tail，添加Handler时，就是将Handler先封装为ChannelHandlerContext，再添加到链表中
     * Handler的每个事件的触发时机就是在不同时候通过Pipeline调用对应的方法，如何循环调用每个Handler
     * 关键入口和处理逻辑都在 AbstractChannelHandlerContext 类中。
     */
    static class MyHandler extends ChannelInboundHandlerAdapter {
        /**
         * 作用：
         *     当读取到数据时调用。msg 是入站数据，通常是通过解码器解码后的消息对象。
         * 调用时机：
         *     当有数据从远程端读取时触发。Netty 将数据从底层 ByteBuf 解码后，通过该方法传递到用户的业务逻辑中。
         */
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("收到客户端发送的消息>>>>>>>>>>>>>>>>>");
            ByteBuf buf = (ByteBuf) msg;
            System.out.println(buf.toString(StandardCharsets.UTF_8));
        }


        /**
         * 作用：
         *     当 Channel 注册到其 EventLoop 上时调用。意味着这个 Channel 准备好处理 I/O 操作。
         * 调用时机：
         *     Channel 被创建后，注册到 EventLoop 时触发。通常会在通道初始化的早期阶段调用。
         *
         */
        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("Channel被创建，注册到 EventLoop =========");
            super.channelRegistered(ctx);
        }

        /**
         * 作用：
         *     当 Channel 从其 EventLoop 注销时调用。此时，Channel 不再处理任何 I/O 操作。
         * 调用时机：
         *     Channel 被关闭或从 EventLoop 中注销时触发，表示通道生命周期即将结束。
         *
         */
        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("Channel被关闭或从EventLoop中注销===========");
            super.channelUnregistered(ctx);
        }

        /**
         * 作用：
         *     当 Channel 处于活动状态时调用，表示通道已经连接并准备好发送和接收数据。
         * 调用时机：
         *     在 TCP 连接建立或连接成功时触发（Channel 进入活动状态）。这表明此时可以进行数据通信。
         */
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("TCP连接建立或连接成功===============");
            super.channelActive(ctx);
        }

        /**
         * 作用：
         *     当Channel变为不活跃状态时调用，表示连接已经断开或者 Channel 关闭。
         * 调用时机：
         *     当 TCP 连接被关闭，Channel 不再活跃时触发。此时不能再发送或接收数据。
         */
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("Channel变为不活跃状态==========");
            super.channelInactive(ctx);
        }

        /**
         * 作用：
         *     当一次读操作完成时调用。
         * 调用时机：
         *     每次读取操作完成（即消息全部读完）时触发。通常在 channelRead() 之后调用，用来标识本次读操作的结束。
         */
        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            System.out.println("读操作完=============");
            super.channelReadComplete(ctx);
        }

        /**
         * 作用：处理用户自定义事件。可以通过 ctx.fireUserEventTriggered() 触发。
         * 调用时机：当用户触发特定事件时，如自定义心跳检测、闲置超时等。
         */
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            super.userEventTriggered(ctx, evt);
        }

        /**
         * 作用：当通道的可写状态发生变化时调用。比如，当缓冲区已满，写操作将被暂停；一旦缓冲区可用，写操作将被恢复。
         * 调用时机：当 Channel 可写性发生变化时触发，通常用于流量控制。
         */
        @Override
        public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            super.channelWritabilityChanged(ctx);
        }

        /**
         * 作用：当处理过程中出现异常时调用。Netty 默认会将异常传播给处理链上的其他 ChannelHandler，直到其中一个处理器处理该异常。
         * 调用时机：当出现异常（如网络错误、解码错误）时触发。
         */
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
        }
    }

}
