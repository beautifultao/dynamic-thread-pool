package com.cumt.atao.dynamic.thread.pool.sdk.config;

import com.cumt.atao.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import com.cumt.atao.dynamic.thread.pool.sdk.domain.entity.ThreadPoolConfigEntity;
import com.cumt.atao.dynamic.thread.pool.sdk.domain.service.DynamicThreadPoolService;
import com.cumt.atao.dynamic.thread.pool.sdk.registry.IRegistry;
import com.cumt.atao.dynamic.thread.pool.sdk.registry.redis.RedisRegistry;
import com.cumt.atao.dynamic.thread.pool.sdk.registry.valobj.RegistryEnumVO;
import com.cumt.atao.dynamic.thread.pool.sdk.trigger.job.ThreadPoolDataReportJob;
import com.cumt.atao.dynamic.thread.pool.sdk.trigger.listener.ThreadPoolConfigAdjustListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;
import java.util.Set;
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
    private String applicationName;

    @Bean("redissonClient")
    public RedissonClient redissonClient(DynamicThreadPoolAutoProperties properties){

        ObjectMapper objectMapper = new ObjectMapper();
        // 禁用多态类型信息，不写入 @class 字段
        objectMapper.deactivateDefaultTyping();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 使用自定义的 ObjectMapper 创建 JsonJacksonCodec
        JsonJacksonCodec codec = new JsonJacksonCodec(objectMapper);

        Config config = new Config();
        config.setCodec(codec);

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
    public DynamicThreadPoolService dynamicThreadPoolService(ApplicationContext applicationContext, Map<String, ThreadPoolExecutor> threadPoolExecutorMap,RedissonClient redissonClient){

        applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");

        if(StringUtil.isBlank(applicationName)){
            applicationName="缺省的";
            log.info("动态线程池启动提示: 未配置应用名称");
        }

        // 获取缓存数据，设置本地线程池配置
        Set<String> threadPoolKeys = threadPoolExecutorMap.keySet();
        for (String threadPoolKey : threadPoolKeys) {
            ThreadPoolConfigEntity threadPoolConfigEntity = (ThreadPoolConfigEntity) redissonClient.getBucket(RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY + "_" + applicationName + "_" + threadPoolKey).get();
            if (null == threadPoolConfigEntity) continue;
            ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(threadPoolKey);
            threadPoolExecutor.setCorePoolSize(threadPoolConfigEntity.getCorePoolSize());
            threadPoolExecutor.setMaximumPoolSize(threadPoolConfigEntity.getMaximumPoolSize());
        }

        return new DynamicThreadPoolService(applicationName, threadPoolExecutorMap);
    }

    @Bean
    public ThreadPoolDataReportJob threadPoolDataReportJob(IDynamicThreadPoolService dynamicThreadPoolService,IRegistry registry){
        return new ThreadPoolDataReportJob(dynamicThreadPoolService,registry);
    }

    @Bean
    public ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener(IDynamicThreadPoolService dynamicThreadPoolService, IRegistry registry){
        return new ThreadPoolConfigAdjustListener(registry, dynamicThreadPoolService);
    }

    @Bean(name = "dynamicThreadPoolRedisTopic")
    public RTopic threadPoolConfigAdjust(RedissonClient redissonClient, ThreadPoolConfigAdjustListener listener){
        RTopic topic = redissonClient.getTopic(RegistryEnumVO.DYNAMIC_THREAD_POOL_REDIS_TOPIC.getKey() + "_" + applicationName);
        topic.addListener(ThreadPoolConfigEntity.class, listener);

        return topic;
    }
}
