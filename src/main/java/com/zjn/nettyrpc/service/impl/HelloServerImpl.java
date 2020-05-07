package com.zjn.nettyrpc.service.impl;

import com.zjn.nettyrpc.service.HelloService;

public class HelloServerImpl implements HelloService {

    @Override
    public String hello(String msg) {
        return msg != null ? msg + " -----> 你好，我是190coder" : "暂时没有消息";
    }
}
