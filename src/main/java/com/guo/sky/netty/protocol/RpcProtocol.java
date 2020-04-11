package com.guo.sky.netty.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * @author GuoTianchi
 * @version 1.0
 * @date 2020/4/10 21:45
 */
@Data
public class RpcProtocol implements Serializable {

    private String className;
    private String methodName;
    private Object[] args;
    private Class<?>[] argsTypes;

}
