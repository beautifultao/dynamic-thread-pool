package com.cumt.atao.dynamic.thread.pool.sdk.config;

import com.alibaba.fastjson2.JSON;
import com.cumt.atao.dynamic.thread.pool.sdk.domain.service.DynamicThreadPoolService;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * @DateTime: 2024/8/9
 * @Description: 动态配置入口
 * @Author: 阿涛
 **/
@Configuration
@Slf4j
public class DynamicThreadPoolAutoConfig {
    @Bean("dynamicThreadPoolService")
    public DynamicThreadPoolService dynamicThreadPoolService(ApplicationContext applicationContext, Map<String, ThreadPoolExecutor> threadPoolExecutorMap){

        String applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");

        if(StringUtil.isBlank(applicationName)){
            applicationName="缺省的";
            log.info("动态线程池启动提示: 未配置应用名称");
        }

        return new DynamicThreadPoolService(applicationName, threadPoolExecutorMap);
    }
}
