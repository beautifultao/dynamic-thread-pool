package com.cumt.atao.dynamic.thread.pool.sdk.domain;

import com.cumt.atao.dynamic.thread.pool.sdk.domain.model.ThreadPoolConfigEntity;

import java.util.List;

/**
 * @DateTime: 2024/8/10
 * @Description: 动态线程池服务
 * @Author: 阿涛
 **/
public interface IDynamicThreadPoolService {
    List<ThreadPoolConfigEntity> queryThreadPoolList();

    ThreadPoolConfigEntity queryThreadPoolConfigByName(String threadPoolName);

    void updateThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity);
}
