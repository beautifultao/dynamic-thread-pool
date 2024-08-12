package com.cumt.atao.dynamic.thread.pool.sdk.trigger.listener;

import com.alibaba.fastjson2.JSON;
import com.cumt.atao.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import com.cumt.atao.dynamic.thread.pool.sdk.domain.entity.ThreadPoolConfigEntity;
import com.cumt.atao.dynamic.thread.pool.sdk.registry.IRegistry;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.listener.MessageListener;

import java.util.List;

/**
 * @DateTime: 2024/8/10
 * @Description: 线程池配置变更监听
 * @Author: 阿涛
 **/
@Slf4j
public class ThreadPoolConfigAdjustListener implements MessageListener<ThreadPoolConfigEntity> {

    private final IRegistry registry;
    private final IDynamicThreadPoolService dynamicThreadPoolService;

    public ThreadPoolConfigAdjustListener(IRegistry registry, IDynamicThreadPoolService dynamicThreadPoolService) {
        this.registry = registry;
        this.dynamicThreadPoolService = dynamicThreadPoolService;
    }

    @Override
    public void onMessage(CharSequence channel, ThreadPoolConfigEntity threadPoolConfigEntity) {
        log.info("动态线程池，调整线程池配置。线程池名称:{} 核心线程数:{} 最大线程数:{}", threadPoolConfigEntity.getThreadPoolName(), threadPoolConfigEntity.getPoolSize(), threadPoolConfigEntity.getMaximumPoolSize());
        dynamicThreadPoolService.updateThreadPoolConfig(threadPoolConfigEntity);

        // 更新后上报最新数据
        List<ThreadPoolConfigEntity> threadPoolConfigEntities = dynamicThreadPoolService.queryThreadPoolList();
        registry.reportThreadPool(threadPoolConfigEntities);

        ThreadPoolConfigEntity threadPoolConfigEntityCurrent = dynamicThreadPoolService.queryThreadPoolConfigByName(threadPoolConfigEntity.getThreadPoolName());
        registry.reportThreadPoolConfigParameter(threadPoolConfigEntityCurrent);
        log.info("动态线程池，上报线程池配置：{}", JSON.toJSONString(threadPoolConfigEntity));
    }
}
