package com.cumt.atao.dynamic.thread.pool.sdk.registry;

import com.cumt.atao.dynamic.thread.pool.sdk.domain.model.ThreadPoolConfigEntity;

import java.util.List;

/**
 * @DateTime: 2024/8/10
 * @Description: Redis注册中心
 * @Author: 阿涛
 **/
public interface IRegistry {
    void reportThreadPool(List<ThreadPoolConfigEntity> threadPoolEntities);

    void reportThreadPoolConfigParameter(ThreadPoolConfigEntity threadPoolConfigEntity);
}
