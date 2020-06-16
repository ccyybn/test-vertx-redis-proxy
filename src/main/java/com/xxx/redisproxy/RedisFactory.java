package com.xxx.redisproxy;

import com.xxx.util.Asserts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import io.vertx.core.Vertx;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

@Component
public class RedisFactory {
    @Resource
    private Vertx vertx;
    @Value("#{environment.REDIS_HOSTS}")
    private String hosts;

    public RedisClient createRedisClient() {
        return new RedisProxy(vertx, createRedisImpl());
    }


    private RedisClient createRedisImpl() {
        String arr[] = hosts.split(",");
        Asserts.assertNotNull(arr, RuntimeException::new);
        String arr2[] = arr[0].split(":");
        Asserts.assertNotNull(arr2, RuntimeException::new);
        Asserts.assertEquals(arr2.length, 2, RuntimeException::new);
        return RedisClient.create(vertx, new RedisOptions().setHost(arr2[0]).setPort(Integer.parseInt(arr2[1])));
    }
}
