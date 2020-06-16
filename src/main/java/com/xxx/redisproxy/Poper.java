package com.xxx.redisproxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.redis.RedisClient;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class Poper {
    @Value("#{environment.MODE}")
    private String MODE;
    @Resource
    RedisClient redis;

    @Resource
    Vertx vertx;

    @PostConstruct
    public void initialize() {
        if (MODE.equals("pop"))
            check();
    }

    public void check() {
        Future<JsonArray> future = Future.future();
        redis.lrange(Pusher.KEY, 0, -1, future.completer());

        future.setHandler(r -> {
            if (r.succeeded()) {
                JsonArray result = r.result();
//                Map<Integer, List<Integer>> collect = result.stream().map(e -> Integer.parseInt(e.toString()))
//                        .sorted(Integer::compareTo).collect(Collectors.groupingBy(i -> i));

                List<Integer> collect = result.stream().map(e -> Integer.parseInt(e.toString()))
                        .sorted(Integer::compareTo).collect(Collectors.toList());
//
//                List<Integer> errorList = collect.entrySet().stream().filter(e -> e.getValue().size() > 1).map(e -> e.getKey()).collect(Collectors.toList());
//                int max = errorList.stream().mapToInt(i -> i).max().getAsInt();
//                int min = errorList.stream().mapToInt(i -> i).min().getAsInt();
//
//                log.debug("min {}", min);
//                log.debug("max {}", max);
//                log.debug("size {}", errorList.size());
//                log.debug("max - min {}", max - min);


                for (int i = 1; i <= collect.size(); i++) {
                    int val = collect.get(i - 1);
                    if (i != val) {
                        log.error("check not pass expect: {} actual: {}", i, val);
                    }
                }
                log.error("check complete!");
            } else log.error("", r.cause());
        });
    }
}
