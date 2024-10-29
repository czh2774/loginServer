package com.example.xiuxianloginserver;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class XiuXianLoginServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiuXianLoginServerApplication.class, args);
    }

    // 使用 @PostConstruct 确保时区在应用启动时被设置为 UTC
    @PostConstruct
    public void init(){
        // 设置 JVM 全局时区为 UTC
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        System.out.println("登录服务器启动，时区设置为: " + TimeZone.getDefault().getID());
    }
}
