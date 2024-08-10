package com.cumt.atao.dynamic.thread.pool.sdk.domain.model;

import lombok.Data;

/**
 * @DateTime: 2024/8/10
 * @Description: TODO(一句话描述此类的作用)
 * @Author: 阿涛
 **/
@Data
public class ThreadPoolConfigEntity {
    /**
     * 应用名称
     */
    private String appName;

    /**
     * 线程池名称
     */
    private String threadPoolName;

    /**
     * 核心线程数
     */
    private int corePoolSize;

    /**
     * 最大线程数
     */
    private int maximumPoolSize;

    /**
     * 当前活跃线程数
     */
    private int activeCount;

    /**
     * 当前池中线程数
     */
    private int poolSize;

    /**
     * 队列类型
     */
    private String queueType;

    /**
     * 当前队列任务数
     */
    private int queueSize;

    /**
     * 队列剩余任务数
     */
    private int remainingCapacity;

    public ThreadPoolConfigEntity(String applicationName, String beanName) {
        this.appName = applicationName;
        this.threadPoolName = beanName;
    }
}
