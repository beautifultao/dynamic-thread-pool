package com.cumt.atao.dynamic.thread.pool.sdk.trigger.job;

import com.alibaba.fastjson2.JSON;
import com.cumt.atao.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import com.cumt.atao.dynamic.thread.pool.sdk.domain.model.ThreadPoolConfigEntity;
import com.cumt.atao.dynamic.thread.pool.sdk.registry.IRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * @DateTime: 2024/8/10
 * @Description: 线程池数据上报任务
 * @Author: 阿涛
 **/
@Slf4j
public class ThreadPoolDataReportJob {
    private final IDynamicThreadPoolService dynamicThreadPoolService;
    private final IRegistry registry;

    public ThreadPoolDataReportJob(IDynamicThreadPoolService dynamicThreadPoolService, IRegistry registry) {
        this.dynamicThreadPoolService = dynamicThreadPoolService;
        this.registry = registry;
    }


    @Scheduled(cron = "0/20 * * * * ?")
    public void execReportThreadList(){
        List<ThreadPoolConfigEntity> threadPoolConfigEntities = dynamicThreadPoolService.queryThreadPoolList();
        registry.reportThreadPool(threadPoolConfigEntities);

        log.info("动态线程池，上报线程池信息：{}", JSON.toJSONString(threadPoolConfigEntities));
        for (ThreadPoolConfigEntity threadPoolConfigEntity : threadPoolConfigEntities) {
            registry.reportThreadPoolConfigParameter(threadPoolConfigEntity);
            log.info("动态线程池，上报线程池配置：{}", JSON.toJSONString(threadPoolConfigEntity));
        }
    }
}
