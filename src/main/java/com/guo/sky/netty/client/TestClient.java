package com.guo.sky.netty.client;

import com.guo.sky.netty.api.HelloService;
import com.guo.sky.netty.protocol.RpcProtocol;

import javax.sound.midi.Soundbank;

/**
 * @author GuoTianchi
 * @version 1.0
 * @date 2020/4/11 15:18
 */
public class TestClient {

    public static void main(String[] args) {
        ClientProxy clientProxy = new ClientProxy();
        HelloService helloService = clientProxy.getInstance(HelloService.class);
        String guo = helloService.say("Guo");
        System.out.println(guo);
    }

}
