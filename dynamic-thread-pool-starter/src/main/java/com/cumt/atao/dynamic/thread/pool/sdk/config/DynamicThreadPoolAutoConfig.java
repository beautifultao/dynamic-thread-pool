package com.cumt.atao.dynamic.thread.pool.sdk.config;

import com.cumt.atao.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import com.cumt.atao.dynamic.thread.pool.sdk.domain.service.DynamicThreadPoolService;
import com.cumt.atao.dynamic.thread.pool.sdk.registry.IRegistry;
import com.cumt.atao.dynamic.thread.pool.sdk.registry.redis.RedisRegistry;
import com.cumt.atao.dynamic.thread.pool.sdk.trigger.job.ThreadPoolDataReportJob;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * @DateTime: 2024/8/9
 * @Description: 动态配置入口
 * @Author: 阿涛
 **/
@Configuration
@EnableScheduling
@Slf4j
@EnableConfigurationProperties(DynamicThreadPoolAutoProperties.class)
public class DynamicThreadPoolAutoConfig {

    @Bean("redissonClient")
    public RedissonClient redissonClient(DynamicThreadPoolAutoProperties properties){
        // 自定义的 JsonJacksonCodec，禁用类信息
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 禁用默认的类型信息
        objectMapper.deactivateDefaultTyping();

        Config config = new Config();
        config.setCodec(new JsonJacksonCodec(objectMapper));

        config.useSingleServer()
                .setAddress("redis://" + properties.getHost() + ":" + properties.getPort())
                .setPassword(properties.getPassword())
                .setConnectionPoolSize(properties.getPoolSize())
                .setConnectionMinimumIdleSize(properties.getMinIdleSize())
                .setIdleConnectionTimeout(properties.getIdleTimeout())
                .setConnectTimeout(properties.getConnectTimeout())
                .setRetryAttempts(properties.getRetryAttempts())
                .setRetryInterval(properties.getRetryInterval())
                .setPingConnectionInterval(properties.getPingInterval())
                .setKeepAlive(properties.isKeepAlive())
        ;

        RedissonClient redissonClient = Redisson.create(config);
        log.info("动态线程池，注册器（redis）链接初始化完成。{} {} {}", properties.getHost(), properties.getPoolSize(), !redissonClient.isShutdown());
        return redissonClient;
    }

    @Bean
    public IRegistry redisRegister(RedissonClient redissonClient){
        return new RedisRegistry(redissonClient);
    }

    @Bean("dynamicThreadPoolService")
    public DynamicThreadPoolService dynamicThreadPoolService(ApplicationContext applicationContext, Map<String, ThreadPoolExecutor> threadPoolExecutorMap){

        String applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");

        if(StringUtil.isBlank(applicationName)){
            applicationName="缺省的";
            log.info("动态线程池启动提示: 未配置应用名称");
        }

        return new DynamicThreadPoolService(applicationName, threadPoolExecutorMap);
    }

    @Bean
    public ThreadPoolDataReportJob threadPoolDataReportJob(IDynamicThreadPoolService dynamicThreadPoolService,IRegistry registry){
        return new ThreadPoolDataReportJob(dynamicThreadPoolService,registry);
    }
}
