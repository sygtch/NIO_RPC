package com.guo.sky.netty.provider;

import com.guo.sky.netty.api.HelloService;

/**
 * @author GuoTianchi
 * @version 1.0
 * @date 2020/4/10 22:30
 */
public class HelloServiceImpl implements HelloService {

    public String say(String name) {
        return "客户端：" + name + " 发来的消息";
    }
}
