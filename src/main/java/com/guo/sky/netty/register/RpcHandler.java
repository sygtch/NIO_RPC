package com.guo.sky.netty.register;

import com.guo.sky.netty.protocol.RpcProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author GuoTianchi
 * @version 1.0
 * @date 2020/4/10 21:45
 */
public class RpcHandler extends ChannelInboundHandlerAdapter {

    private List<String> scanClassName = new ArrayList<String>();
    private Map<String,Object> registerMap = new HashMap<String, Object>();

    public RpcHandler() {
        doScan("com.guo.sky.netty.provider");
        doRegister();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        RpcProtocol protocol = (RpcProtocol)msg;
        String className = protocol.getClassName();
        Object result = null;
        if (registerMap.containsKey(className)){
            Object obj  = registerMap.get(className);
            Class<?> clazz = Class.forName(className);
            Method method = clazz.getMethod(protocol.getMethodName(), protocol.getArgsTypes());
            result = method.invoke(obj, protocol.getArgs());
        }
        ctx.write(result);
        ctx.flush();
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private void doScan(String path) {
        String realPath = path.replaceAll("\\.","/");
        URL resource = getClass().getClassLoader().getResource(realPath);
        File file = new File(resource.getFile());
        for (File f : file.listFiles()) {
            if(f.isDirectory()){
                doScan(path + "." + f.getName());
            } else {
                scanClassName.add(path + "." + f.getName().replaceAll(".class","").trim());
            }
        }
    }

    private void doRegister() {
        if (scanClassName == null){
            return;
        }
        for (String s : scanClassName) {
            try {
                Class<?> clazz = Class.forName(s);
                Class<?> inter = clazz.getInterfaces()[0];
                registerMap.put(inter.getName(),clazz.newInstance());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

}
