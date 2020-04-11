package com.guo.sky.netty.client;

import com.guo.sky.netty.protocol.RpcProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author GuoTianchi
 * @version 1.0
 * @date 2020/4/11 14:27
 */
public class ClientProxy implements InvocationHandler {

    private  Class<?> clazz;

    /**
     * 动态代理
     * @param clazz
     * @param <T>
     * @return
     */
    public  <T> T getInstance(Class<T> clazz){
        this.clazz = clazz;
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),new Class<?>[]{clazz},this);
    }

    /**
     * 调用方法
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        RpcProtocol protocol = new RpcProtocol();
        protocol.setClassName(clazz.getName());
        protocol.setMethodName(method.getName());
        protocol.setArgs(args);
        protocol.setArgsTypes(method.getParameterTypes());
        Object response = connect(protocol);
        return response;
    }

    private Object connect(RpcProtocol protocol){
        Bootstrap bootstrap = new Bootstrap();
        final ClientHandler handler = new ClientHandler();
        EventLoopGroup work = new NioEventLoopGroup();
        bootstrap.group(work)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY,true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                        //自定义协议编码器
                        pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                        //对象参数类型编码器
                        pipeline.addLast("encoder", new ObjectEncoder());
                        //对象参数类型解码器
                        pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                        pipeline.addLast("handler",handler);
                    }
                });
        try {
            ChannelFuture future = bootstrap.connect("localhost", 8080).sync();
            Channel channel = future.channel();
            channel.writeAndFlush(protocol).sync();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            work.shutdownGracefully();
        }
        return handler.getResponse();
    }
}
