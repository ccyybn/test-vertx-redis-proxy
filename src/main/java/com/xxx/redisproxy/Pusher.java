package com.xxx.redisproxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.redis.RedisClient;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class Pusher {
    @Value("#{environment.MODE}")
    private String MODE;
    public static String KEY = "test-1";

    @Resource
    RedisClient redis;

    @Resource
    Vertx vertx;

    @PostConstruct
    public void initialize() {
        if (MODE.equals("push")) {
            final int[] i = {0};
            vertx.setPeriodic(1000, l -> {
                List<RedisCMD> cmds = new ArrayList<>();
                int start = i[0] + 1;
                for (int j = 0; j < 1000; j++) {
                    i[0]++;
                    String val = String.valueOf(i[0]);
                    cmds.add(RedisCMD.buildRPUSH(KEY, val));
                }
                int end = i[0];
                log.debug("rpush try! {}->{}", start, end);
                Future<JsonArray> future = RedisScriptUtil.executeCMD(redis, cmds);
                future.setHandler(r -> {
                    if (r.succeeded()) {
                        log.debug("rpush success! {}->{}", start, end);
                    } else log.error("rpush {}->{}", start, end, r.cause());
                });
            });
        }
    }
}
