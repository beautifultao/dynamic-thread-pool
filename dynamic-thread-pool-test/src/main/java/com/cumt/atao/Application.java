package com.cumt.atao;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @DateTime: 2024/8/9
 * @Description: TODO(一句话描述此类的作用)
 * @Author: 阿涛
 **/
@SpringBootApplication
@Configurable
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}