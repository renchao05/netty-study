package com.renchao;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;

public class Test02_NioEventLoo {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        EventLoop eventLoop = group.next();
        // 第一次调用execute方法的时候，会启动工作线程
        eventLoop.execute(() -> {
            System.out.println("Hello World");
            System.out.println(Thread.currentThread().getName());
        });
    }


}
