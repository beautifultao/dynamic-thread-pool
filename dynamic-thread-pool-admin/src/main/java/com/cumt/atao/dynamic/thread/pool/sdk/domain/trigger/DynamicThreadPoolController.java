package com.cumt.atao.dynamic.thread.pool.sdk.domain.trigger;

import com.alibaba.fastjson2.JSON;
import com.cumt.atao.dynamic.thread.pool.sdk.domain.entity.ThreadPoolConfigEntity;
import com.cumt.atao.dynamic.thread.pool.sdk.domain.types.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @DateTime: 2024/8/10
 * @Description: TODO(一句话描述此类的作用)
 * @Author: 阿涛
 **/
@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/dynamic/thread/pool/")
public class DynamicThreadPoolController {
    @Resource
    public RedissonClient redissonClient;

    /**
     * 查询线程池数据
     * --url 'http://localhost:8089/api/v1/dynamic/thread/pool/query_thread_pool_list'
     */
    @GetMapping(value = "query_thread_pool_list")
    public Response<List<ThreadPoolConfigEntity>> queryThreadPoolList(){
        try {
            RList<ThreadPoolConfigEntity> cacheList = redissonClient.getList("THREAD_POOL_CONFIG_LIST_KEY");

            return Response.<List<ThreadPoolConfigEntity>>builder()
                    .code(Response.Code.SUCCESS.getCode())
                    .info(Response.Code.SUCCESS.getInfo())
                    .data(cacheList.readAll())
                    .build();
        } catch (Exception e) {
            log.error("查询线程池数据异常", e);
            return Response.<List<ThreadPoolConfigEntity>>builder()
                    .code(Response.Code.UN_ERROR.getCode())
                    .info(Response.Code.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 查询线程池配置
     * --url 'http://localhost:8089/api/v1/dynamic/thread/pool/query_thread_pool_config?appName=dynamic-thread-pool-test-app&threadPoolName=threadPoolExecutor'
     */
    @GetMapping(value = "query_thread_pool_config")
    public Response<ThreadPoolConfigEntity> queryThreadPoolConfigByName(@RequestParam String appName, @RequestParam String threadPoolName){
        try{
            String cacheKey = "THREAD_POOL_CONFIG_PARAMETER_LIST_KEY" + "_" + appName + "_" + threadPoolName;
            ThreadPoolConfigEntity threadPoolConfigEntity = redissonClient.<ThreadPoolConfigEntity>getBucket(cacheKey).get();
            return Response.<ThreadPoolConfigEntity>builder()
                    .code(Response.Code.SUCCESS.getCode())
                    .info(Response.Code.SUCCESS.getInfo())
                    .data(threadPoolConfigEntity)
                    .build();

        }catch (Exception e){
            log.error("查询线程池配置异常", e);
            return Response.<ThreadPoolConfigEntity>builder()
                    .code(Response.Code.UN_ERROR.getCode())
                    .info(Response.Code.UN_ERROR.getInfo())
                    .build();

        }
    }

    @PostMapping(value =  "update_thread_pool_config")
    public Response<Boolean> updateThreadPoolConfig(@RequestBody ThreadPoolConfigEntity request) {
        try {
            log.info("修改线程池配置开始 {} {} {}", request.getAppName(), request.getThreadPoolName(), JSON.toJSONString(request));
            RTopic topic = redissonClient.getTopic("DYNAMIC_THREAD_POOL_REDIS_TOPIC" + "_" + request.getAppName());
            topic.publish(request);
            log.info("修改线程池配置完成 {} {}", request.getAppName(), request.getThreadPoolName());
            return Response.<Boolean>builder()
                    .code(Response.Code.SUCCESS.getCode())
                    .info(Response.Code.SUCCESS.getInfo())
                    .data(true)
                    .build();
        } catch (Exception e) {
            log.error("修改线程池配置异常 {}", JSON.toJSONString(request), e);
            return Response.<Boolean>builder()
                    .code(Response.Code.UN_ERROR.getCode())
                    .info(Response.Code.UN_ERROR.getInfo())
                    .data(false)
                    .build();
        }
    }
}
