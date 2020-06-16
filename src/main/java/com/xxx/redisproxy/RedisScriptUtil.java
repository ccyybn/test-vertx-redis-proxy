package com.xxx.redisproxy;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.redis.RedisClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedisScriptUtil {
    private static final Map<String, String> contentMap;
    private static final Gson GSON = new Gson();

    static {
        //TODO: Spring initialization is better
        // we can use config file here
        Map<String, String> path = new HashMap<>();
        path.put("execute_cmd", "/lua/execute_cmd.lua");
        contentMap = path.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, k -> readFileContent(k.getValue())));
    }

    private static String readFileContent(String filePath) {
        String content;
        ClassPathResource resource = new ClassPathResource(filePath);
        try (InputStream is = resource.getInputStream()) {
            content = IOUtils.toString(is);
        } catch (IOException e) {
            log.error("exception occurs: {}", e);
            throw new RuntimeException(e);
        }
        return content;
    }

    public static Future<JsonArray> executeCMD(RedisClient redis, List<RedisCMD> cmds) {
        List<String> args = cmds.stream().map(GSON::toJson).collect(Collectors.toList());
        Future<JsonArray> future = Future.future();
        redis.eval(contentMap.get("execute_cmd"), ImmutableList.of(), args, future.completer());
//        if (!cmds.isEmpty()) log.debug("[REDIS] execute cmd: {}", args);
        future.setHandler(r -> {
            if (r.failed()) log.error("", r.cause());
        });
        return future;
    }
}
