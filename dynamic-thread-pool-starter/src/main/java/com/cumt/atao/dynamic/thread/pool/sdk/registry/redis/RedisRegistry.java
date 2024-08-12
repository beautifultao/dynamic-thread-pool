package com.cumt.atao.dynamic.thread.pool.sdk.registry.redis;

import com.cumt.atao.dynamic.thread.pool.sdk.domain.entity.ThreadPoolConfigEntity;
import com.cumt.atao.dynamic.thread.pool.sdk.registry.IRegistry;
import com.cumt.atao.dynamic.thread.pool.sdk.registry.valobj.RegistryEnumVO;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.List;

/**
 * @DateTime: 2024/8/10
 * @Description: redis注册中心服务
 * @Author: 阿涛
 **/
public class RedisRegistry implements IRegistry {
    private final RedissonClient redissonClient;

    public RedisRegistry(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void reportThreadPool(List<ThreadPoolConfigEntity> threadPoolEntities) {
        RList<ThreadPoolConfigEntity> list = redissonClient.getList(RegistryEnumVO.THREAD_POOL_CONFIG_LIST_KEY.getKey());

        list.delete();
        list.addAll(threadPoolEntities);
    }

    @Override
    public void reportThreadPoolConfigParameter(ThreadPoolConfigEntity threadPoolConfigEntity) {
        String cacheKey = RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "_" + threadPoolConfigEntity.getAppName() + "_" + threadPoolConfigEntity.getThreadPoolName();
        RBucket<ThreadPoolConfigEntity> bucket = redissonClient.getBucket(cacheKey);
        bucket.set(threadPoolConfigEntity, Duration.ofDays(30));
    }
}
