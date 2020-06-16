package com.xxx.redisproxy;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisTransaction;
import io.vertx.redis.op.AggregateOptions;
import io.vertx.redis.op.BitFieldOptions;
import io.vertx.redis.op.BitFieldOverflowOptions;
import io.vertx.redis.op.BitOperation;
import io.vertx.redis.op.ClientReplyOptions;
import io.vertx.redis.op.FailoverOptions;
import io.vertx.redis.op.GeoMember;
import io.vertx.redis.op.GeoRadiusOptions;
import io.vertx.redis.op.GeoUnit;
import io.vertx.redis.op.InsertOptions;
import io.vertx.redis.op.KillFilter;
import io.vertx.redis.op.LimitOptions;
import io.vertx.redis.op.MigrateOptions;
import io.vertx.redis.op.ObjectCmd;
import io.vertx.redis.op.RangeLimitOptions;
import io.vertx.redis.op.RangeOptions;
import io.vertx.redis.op.ResetOptions;
import io.vertx.redis.op.ScanOptions;
import io.vertx.redis.op.ScriptDebugOptions;
import io.vertx.redis.op.SetOptions;
import io.vertx.redis.op.SlotCmd;
import io.vertx.redis.op.SortOptions;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedisProxy implements RedisClient {
    private static final long RETRY_PERIOD = 1000L;
    private RedisClient redisClient;
    private Vertx vertx;

    public RedisProxy(Vertx vertx, RedisClient redisClient) {
        this.redisClient = redisClient;
        this.vertx = vertx;
    }

    private <T> RedisClient common(Handler<AsyncResult<T>> handler, Consumer<Handler<AsyncResult<T>>> consumer) {
        Future<T> future = Future.future();
        consumer.accept(future.completer());
        future.setHandler(r -> {
            if (r.succeeded()) {
                handler.handle(Future.succeededFuture(r.result()));
            } else {
                log.error("redis retry after {} ms", RETRY_PERIOD, r.cause());
                vertx.setTimer(RETRY_PERIOD, l -> vertx.runOnContext(v -> common(handler, consumer)));
            }
        });
        return this;
    }

    @Override
    public RedisClient get(String key, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.get(key, f));
    }

    @Override
    public RedisClient getBinary(String key, Handler<AsyncResult<Buffer>> handler) {
        return common(handler, (Handler<AsyncResult<Buffer>> f) -> redisClient.getBinary(key, f));
    }

    @Override
    public RedisClient getbit(String key, long offset, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.getbit(key, offset, f));
    }

    @Override
    public RedisClient getrange(String key, long start, long end, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.getrange(key, start, end, f));
    }

    @Override
    public RedisClient getset(String key, String value, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.getset(key, value, f));
    }

    @Override
    public RedisClient hdel(String key, String field, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.hdel(key, field, f));
    }

    @Override
    public RedisClient hdelMany(String key, List<String> fields, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.hdelMany(key, fields, f));
    }

    @Override
    public RedisClient hexists(String key, String field, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.hexists(key, field, f));
    }

    @Override
    public RedisClient hget(String key, String field, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.hget(key, field, f));
    }

    @Override
    public RedisClient hgetall(String key, Handler<AsyncResult<JsonObject>> handler) {
        return common(handler, (Handler<AsyncResult<JsonObject>> f) -> redisClient.hgetall(key, f));
    }

    @Override
    public RedisClient hincrby(String key, String field, long increment, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.hincrby(key, field, increment, f));
    }

    @Override
    public RedisClient hincrbyfloat(String key, String field, double increment, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.hincrbyfloat(key, field, increment, f));
    }

    @Override
    public RedisClient hkeys(String key, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.hkeys(key, f));
    }

    @Override
    public RedisClient hlen(String key, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.hlen(key, f));
    }

    @Override
    public RedisClient hmget(String key, List<String> fields, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.hmget(key, fields, f));
    }

    @Override
    public RedisClient hmset(String key, JsonObject values, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.hmset(key, values, f));
    }

    @Override
    public RedisClient hset(String key, String field, String value, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.hset(key, field, value, f));
    }

    @Override
    public RedisClient hsetnx(String key, String field, String value, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.hsetnx(key, field, value, f));
    }

    @Override
    public RedisClient hvals(String key, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.hvals(key, f));
    }

    @Override
    public RedisClient incr(String key, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.incr(key, f));
    }

    @Override
    public RedisClient incrby(String key, long increment, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.incrby(key, increment, f));
    }

    @Override
    public RedisClient incrbyfloat(String key, double increment, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.incrbyfloat(key, increment, f));
    }

    @Override
    public RedisClient info(Handler<AsyncResult<JsonObject>> handler) {
        return common(handler, (Handler<AsyncResult<JsonObject>> f) -> redisClient.info(f));
    }

    @Override
    public RedisClient infoSection(String section, Handler<AsyncResult<JsonObject>> handler) {
        return common(handler, (Handler<AsyncResult<JsonObject>> f) -> redisClient.infoSection(section, f));
    }

    @Override
    public RedisClient keys(String pattern, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.keys(pattern, f));
    }

    @Override
    public RedisClient lastsave(Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.lastsave(f));
    }

    @Override
    public RedisClient lindex(String key, int index, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.lindex(key, index, f));
    }

    @Override
    public RedisClient linsert(String key, InsertOptions option, String pivot, String value, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.linsert(key, option, pivot, value, f));
    }

    @Override
    public RedisClient llen(String key, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.llen(key, f));
    }

    @Override
    public RedisClient rpush(String key, String value, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.rpush(key, value, f));
    }

    @Override
    public RedisClient rpushx(String key, String value, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.rpushx(key, value, f));
    }

    @Override
    public RedisClient sadd(String key, String member, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.sadd(key, member, f));
    }

    @Override
    public RedisClient saddMany(String key, List<String> members, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.saddMany(key, members, f));
    }

    @Override
    public RedisClient save(Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.save(f));
    }

    @Override
    public RedisClient scard(String key, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.scard(key, f));
    }

    @Override
    public RedisClient scriptExists(String script, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.scriptExists(script, f));
    }

    @Override
    public RedisClient scriptExistsMany(List<String> scripts, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.scriptExistsMany(scripts, f));
    }

    @Override
    public RedisClient scriptFlush(Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.scriptFlush(f));
    }

    @Override
    public RedisClient scriptKill(Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.scriptKill(f));
    }

    @Override
    public RedisClient scriptLoad(String script, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.scriptLoad(script, f));
    }

    @Override
    public RedisClient sdiff(String key, List<String> cmpkeys, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.sdiff(key, cmpkeys, f));
    }

    @Override
    public RedisClient sdiffstore(String destkey, String key, List<String> cmpkeys, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.sdiffstore(destkey, key, cmpkeys, f));
    }

    @Override
    public RedisClient select(int dbindex, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.select(dbindex, f));
    }

    @Override
    public RedisClient set(String key, String value, Handler<AsyncResult<Void>> handler) {
        return common(handler, (Handler<AsyncResult<Void>> f) -> redisClient.set(key, value, f));
    }

    @Override
    public RedisClient setWithOptions(String key, String value, SetOptions options, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.setWithOptions(key, value, options, f));
    }

    @Override
    public RedisClient setBinary(String key, Buffer value, Handler<AsyncResult<Void>> handler) {
        return common(handler, (Handler<AsyncResult<Void>> f) -> redisClient.setBinary(key, value, f));
    }

    @Override
    public RedisClient setBinaryWithOptions(String key, Buffer value, SetOptions options, Handler<AsyncResult<Void>> handler) {
        return common(handler, (Handler<AsyncResult<Void>> f) -> redisClient.setBinaryWithOptions(key, value, options, f));
    }

    @Override
    public RedisClient setbit(String key, long offset, int bit, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.setbit(key, offset, bit, f));
    }

    @Override
    public RedisClient setex(String key, long seconds, String value, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.setex(key, seconds, value, f));
    }

    @Override
    public RedisClient setnx(String key, String value, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.setnx(key, value, f));
    }

    @Override
    public RedisClient setrange(String key, int offset, String value, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.setrange(key, offset, value, f));
    }

    @Override
    public RedisClient sinter(List<String> keys, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.sinter(keys, f));
    }

    @Override
    public RedisClient sinterstore(String destkey, List<String> keys, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.sinterstore(destkey, keys, f));
    }

    @Override
    public RedisClient sismember(String key, String member, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.sismember(key, member, f));
    }

    @Override
    public RedisClient slaveof(String host, int port, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.slaveof(host, port, f));
    }

    @Override
    public RedisClient slaveofNoone(Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.slaveofNoone(f));
    }

    @Override
    public RedisClient slowlogGet(int limit, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.slowlogGet(limit, f));
    }

    @Override
    public RedisClient slowlogLen(Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.slowlogLen(f));
    }

    @Override
    public RedisClient slowlogReset(Handler<AsyncResult<Void>> handler) {
        return common(handler, (Handler<AsyncResult<Void>> f) -> redisClient.slowlogReset(f));
    }

    @Override
    public RedisClient smembers(String key, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.smembers(key, f));
    }

    @Override
    public RedisClient smove(String key, String destkey, String member, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.smove(key, destkey, member, f));
    }

    @Override
    public RedisClient sort(String key, SortOptions options, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.sort(key, options, f));
    }

    @Override
    public RedisClient spop(String key, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.spop(key, f));
    }

    @Override
    public RedisClient spopMany(String key, int count, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.spopMany(key, count, f));
    }

    @Override
    public RedisClient srandmember(String key, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.srandmember(key, f));
    }

    @Override
    public RedisClient srandmemberCount(String key, int count, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.srandmemberCount(key, count, f));
    }

    @Override
    public RedisClient srem(String key, String member, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.srem(key, member, f));
    }

    @Override
    public RedisClient sremMany(String key, List<String> members, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.sremMany(key, members, f));
    }

    @Override
    public RedisClient strlen(String key, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.strlen(key, f));
    }

    @Override
    public RedisClient subscribe(String channel, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.subscribe(channel, f));
    }

    @Override
    public RedisClient subscribeMany(List<String> channels, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.subscribeMany(channels, f));
    }

    @Override
    public RedisClient sunion(List<String> keys, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.sunion(keys, f));
    }

    @Override
    public RedisClient sunionstore(String destkey, List<String> keys, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.sunionstore(destkey, keys, f));
    }

    @Override
    public RedisClient sync(Handler<AsyncResult<Void>> handler) {
        return common(handler, (Handler<AsyncResult<Void>> f) -> redisClient.sync(f));
    }

    @Override
    public RedisClient time(Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.time(f));
    }

    @Override
    public RedisTransaction transaction() {
        return redisClient.transaction();
    }

    @Override
    public RedisClient ttl(String key, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.ttl(key, f));
    }

    @Override
    public RedisClient type(String key, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.type(key, f));
    }

    @Override
    public RedisClient unsubscribe(List<String> channels, Handler<AsyncResult<Void>> handler) {
        return common(handler, (Handler<AsyncResult<Void>> f) -> redisClient.unsubscribe(channels, f));
    }

    @Override
    public RedisClient wait(long numSlaves, long timeout, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.wait(numSlaves, timeout, f));
    }

    @Override
    public RedisClient zadd(String key, double score, String member, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.zadd(key, score, member, f));
    }

    @Override
    public RedisClient zaddMany(String key, Map<String, Double> members, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.zaddMany(key, members, f));
    }

    @Override
    public RedisClient zcard(String key, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.zcard(key, f));
    }

    @Override
    public RedisClient zcount(String key, double min, double max, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.zcount(key, min, max, f));
    }

    @Override
    public RedisClient zincrby(String key, double increment, String member, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.zincrby(key, increment, member, f));
    }

    @Override
    public RedisClient zinterstore(String destkey, List<String> sets, AggregateOptions options, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.zinterstore(destkey, sets, options, f));
    }

    @Override
    public RedisClient zinterstoreWeighed(String destkey, Map<String, Double> sets, AggregateOptions options, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.zinterstoreWeighed(destkey, sets, options, f));
    }

    @Override
    public RedisClient zlexcount(String key, String min, String max, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.zlexcount(key, min, max, f));
    }

    @Override
    public RedisClient zrange(String key, long start, long stop, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.zrange(key, start, stop, f));
    }

    @Override
    public RedisClient zrangeWithOptions(String key, long start, long stop, RangeOptions options, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.zrangeWithOptions(key, start, stop, options, f));
    }

    @Override
    public RedisClient zrangebylex(String key, String min, String max, LimitOptions options, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.zrangebylex(key, min, max, options, f));
    }

    @Override
    public RedisClient zrangebyscore(String key, String min, String max, RangeLimitOptions options, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.zrangebyscore(key, min, max, options, f));
    }

    @Override
    public RedisClient zrank(String key, String member, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.zrank(key, member, f));
    }

    @Override
    public RedisClient zrem(String key, String member, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.zrem(key, member, f));
    }

    @Override
    public RedisClient zremMany(String key, List<String> members, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.zremMany(key, members, f));
    }

    @Override
    public RedisClient zremrangebylex(String key, String min, String max, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.zremrangebylex(key, min, max, f));
    }

    @Override
    public RedisClient zremrangebyrank(String key, long start, long stop, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.zremrangebyrank(key, start, stop, f));
    }

    @Override
    public RedisClient zremrangebyscore(String key, String min, String max, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.zremrangebyscore(key, min, max, f));
    }

    @Override
    public RedisClient zrevrange(String key, long start, long stop, RangeOptions options, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.zrevrange(key, start, stop, options, f));
    }

    @Override
    public RedisClient zrevrangebylex(String key, String max, String min, LimitOptions options, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.zrevrangebylex(key, max, min, options, f));
    }

    @Override
    public RedisClient zrevrangebyscore(String key, String max, String min, RangeLimitOptions options, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.zrevrangebyscore(key, max, min, options, f));
    }

    @Override
    public RedisClient zrevrank(String key, String member, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.zrevrank(key, member, f));
    }

    @Override
    public RedisClient zscore(String key, String member, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.zscore(key, member, f));
    }

    @Override
    public RedisClient zunionstore(String destkey, List<String> sets, AggregateOptions options, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.zunionstore(destkey, sets, options, f));
    }

    @Override
    public RedisClient zunionstoreWeighed(String key, Map<String, Double> sets, AggregateOptions options, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.zunionstoreWeighed(key, sets, options, f));
    }

    @Override
    public RedisClient scan(String cursor, ScanOptions options, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.scan(cursor, options, f));
    }

    @Override
    public RedisClient sscan(String key, String cursor, ScanOptions options, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.sscan(key, cursor, options, f));
    }

    @Override
    public RedisClient hscan(String key, String cursor, ScanOptions options, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.hscan(key, cursor, options, f));
    }

    @Override
    public RedisClient zscan(String key, String cursor, ScanOptions options, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.zscan(key, cursor, options, f));
    }

    @Override
    public RedisClient geoadd(String key, double longitude, double latitude, String member, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.geoadd(key, longitude, latitude, member, f));
    }

    @Override
    public RedisClient geoaddMany(String key, List<GeoMember> members, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.geoaddMany(key, members, f));
    }

    @Override
    public RedisClient geohash(String key, String member, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.geohash(key, member, f));
    }

    @Override
    public RedisClient geohashMany(String key, List<String> members, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.geohashMany(key, members, f));
    }

    @Override
    public RedisClient geopos(String key, String member, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.geopos(key, member, f));
    }

    @Override
    public RedisClient geoposMany(String key, List<String> members, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.geoposMany(key, members, f));
    }

    @Override
    public RedisClient geodist(String key, String member1, String member2, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.geodist(key, member1, member2, f));
    }

    @Override
    public RedisClient geodistWithUnit(String key, String member1, String member2, GeoUnit unit, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.geodistWithUnit(key, member1, member2, unit, f));
    }

    @Override
    public RedisClient georadius(String key, double longitude, double latitude, double radius, GeoUnit unit, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.georadius(key, longitude, latitude, radius, unit, f));
    }

    @Override
    public RedisClient georadiusWithOptions(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusOptions options, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.georadiusWithOptions(key, longitude, latitude, radius, unit, options, f));
    }

    @Override
    public RedisClient georadiusbymember(String key, String member, double radius, GeoUnit unit, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.georadiusbymember(key, member, radius, unit, f));
    }

    @Override
    public RedisClient georadiusbymemberWithOptions(String key, String member, double radius, GeoUnit unit, GeoRadiusOptions options, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.georadiusbymemberWithOptions(key, member, radius, unit, options, f));
    }

    @Override
    public RedisClient clientReply(ClientReplyOptions options, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.clientReply(options, f));
    }

    @Override
    public RedisClient hstrlen(String key, String field, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.hstrlen(key, field, f));
    }

    @Override
    public RedisClient touch(String key, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.touch(key, f));
    }

    @Override
    public RedisClient touchMany(List<String> keys, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.touchMany(keys, f));
    }

    @Override
    public RedisClient scriptDebug(ScriptDebugOptions scriptDebugOptions, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.scriptDebug(scriptDebugOptions, f));
    }

    @Override
    public RedisClient bitfield(String key, BitFieldOptions bitFieldOptions, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.bitfield(key, bitFieldOptions, f));
    }

    @Override
    public RedisClient bitfieldWithOverflow(String key, BitFieldOptions commands, BitFieldOverflowOptions overflow, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.bitfieldWithOverflow(key, commands, overflow, f));
    }

    @Override
    public RedisProxy lpop(String key, Handler<AsyncResult<String>> handler) {
        return null;
    }

    @Override
    public RedisClient lpushMany(String key, List<String> values, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.lpushMany(key, values, f));
    }

    @Override
    public RedisClient lpush(String key, String value, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.lpush(key, value, f));
    }

    @Override
    public RedisClient lpushx(String key, String value, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.lpushx(key, value, f));
    }

    @Override
    public RedisClient lrange(String key, long from, long to, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.lrange(key, from, to, f));
    }

    @Override
    public RedisClient lrem(String key, long count, String value, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.lrem(key, count, value, f));
    }

    @Override
    public RedisClient lset(String key, long index, String value, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.lset(key, index, value, f));
    }

    @Override
    public RedisClient ltrim(String key, long from, long to, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.ltrim(key, from, to, f));
    }

    @Override
    public RedisClient mget(String key, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.mget(key, f));
    }

    @Override
    public RedisClient mgetMany(List<String> keys, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.mgetMany(keys, f));
    }

    @Override
    public RedisClient migrate(String host, int port, String key, int destdb, long timeout, MigrateOptions options, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.migrate(host, port, key, destdb, timeout, options, f));
    }

    @Override
    public RedisClient monitor(Handler<AsyncResult<Void>> handler) {
        return common(handler, (Handler<AsyncResult<Void>> f) -> redisClient.monitor(f));
    }

    @Override
    public RedisClient move(String key, int destdb, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.move(key, destdb, f));
    }

    @Override
    public RedisClient mset(JsonObject keyvals, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.mset(keyvals, f));
    }

    @Override
    public RedisClient msetnx(JsonObject keyvals, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.msetnx(keyvals, f));
    }

    @Override
    public RedisClient object(String key, ObjectCmd cmd, Handler<AsyncResult<Void>> handler) {
        return common(handler, (Handler<AsyncResult<Void>> f) -> redisClient.object(key, cmd, f));
    }

    @Override
    public RedisClient persist(String key, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.persist(key, f));
    }

    @Override
    public RedisClient pexpire(String key, long millis, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.pexpire(key, millis, f));
    }

    @Override
    public RedisClient pexpireat(String key, long millis, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.pexpireat(key, millis, f));
    }

    @Override
    public RedisClient pfadd(String key, String element, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.pfadd(key, element, f));
    }

    @Override
    public RedisClient pfaddMany(String key, List<String> elements, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.pfaddMany(key, elements, f));
    }

    @Override
    public RedisClient pfcount(String key, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.pfcount(key, f));
    }

    @Override
    public RedisClient pfcountMany(List<String> keys, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.pfcountMany(keys, f));
    }

    @Override
    public RedisClient pfmerge(String destkey, List<String> keys, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.pfmerge(destkey, keys, f));
    }

    @Override
    public RedisClient ping(Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.ping(f));
    }

    @Override
    public RedisClient psetex(String key, long millis, String value, Handler<AsyncResult<Void>> handler) {
        return common(handler, (Handler<AsyncResult<Void>> f) -> redisClient.psetex(key, millis, value, f));
    }

    @Override
    public RedisClient psubscribe(String pattern, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.psubscribe(pattern, f));
    }

    @Override
    public RedisClient psubscribeMany(List<String> patterns, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.psubscribeMany(patterns, f));
    }

    @Override
    public RedisClient pubsubChannels(String pattern, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.pubsubChannels(pattern, f));
    }

    @Override
    public RedisClient pubsubNumsub(List<String> channels, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.pubsubNumsub(channels, f));
    }

    @Override
    public RedisClient pubsubNumpat(Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.pubsubNumpat(f));
    }

    @Override
    public RedisClient pttl(String key, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.pttl(key, f));
    }

    @Override
    public RedisClient publish(String channel, String message, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.publish(channel, message, f));
    }

    @Override
    public RedisClient punsubscribe(List<String> patterns, Handler<AsyncResult<Void>> handler) {
        return common(handler, (Handler<AsyncResult<Void>> f) -> redisClient.punsubscribe(patterns, f));
    }

    @Override
    public RedisClient randomkey(Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.randomkey(f));
    }

    @Override
    public RedisClient rename(String key, String newkey, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.rename(key, newkey, f));
    }

    @Override
    public RedisClient renamenx(String key, String newkey, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.renamenx(key, newkey, f));
    }

    @Override
    public RedisClient restore(String key, long millis, String serialized, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.restore(key, millis, serialized, f));
    }

    @Override
    public RedisClient role(Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.role(f));
    }

    @Override
    public RedisClient rpop(String key, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.rpop(key, f));
    }

    @Override
    public RedisClient rpoplpush(String key, String destkey, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.rpoplpush(key, destkey, f));
    }

    @Override
    public RedisClient rpushMany(String key, List<String> values, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.rpushMany(key, values, f));
    }

    @Override
    public void close(Handler<AsyncResult<Void>> handler) {

    }

    @Override
    public RedisClient append(String key, String value, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.append(key, value, f));
    }

    @Override
    public RedisClient auth(String password, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.auth(password, f));
    }

    @Override
    public RedisClient bgrewriteaof(Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.bgrewriteaof(f));
    }

    @Override
    public RedisClient bgsave(Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.bgsave(f));
    }

    @Override
    public RedisClient bitcount(String key, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.bitcount(key, f));
    }

    @Override
    public RedisClient bitcountRange(String key, long start, long end, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.bitcountRange(key, start, end, f));
    }

    @Override
    public RedisClient bitop(BitOperation operation, String destkey, List<String> keys, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.bitop(operation, destkey, keys, f));
    }

    @Override
    public RedisClient bitpos(String key, int bit, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.bitpos(key, bit, f));
    }

    @Override
    public RedisClient bitposFrom(String key, int bit, int start, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.bitposFrom(key, bit, start, f));
    }

    @Override
    public RedisClient bitposRange(String key, int bit, int start, int stop, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.bitposRange(key, bit, start, stop, f));
    }

    @Override
    public RedisClient blpop(String key, int seconds, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.blpop(key, seconds, f));
    }

    @Override
    public RedisClient blpopMany(List<String> keys, int seconds, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.blpopMany(keys, seconds, f));
    }

    @Override
    public RedisClient brpop(String key, int seconds, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.brpop(key, seconds, f));
    }

    @Override
    public RedisClient brpopMany(List<String> keys, int seconds, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.brpopMany(keys, seconds, f));
    }

    @Override
    public RedisClient brpoplpush(String key, String destkey, int seconds, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.brpoplpush(key, destkey, seconds, f));
    }

    @Override
    public RedisClient clientKill(KillFilter filter, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.clientKill(filter, f));
    }

    @Override
    public RedisClient clientList(Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.clientList(f));
    }

    @Override
    public RedisClient clientGetname(Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.clientGetname(f));
    }

    @Override
    public RedisClient clientPause(long millis, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.clientPause(millis, f));
    }

    @Override
    public RedisClient clientSetname(String name, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.clientSetname(name, f));
    }

    @Override
    public RedisClient clusterAddslots(List<Long> slots, Handler<AsyncResult<Void>> handler) {
        return common(handler, (Handler<AsyncResult<Void>> f) -> redisClient.clusterAddslots(slots, f));
    }

    @Override
    public RedisClient clusterCountFailureReports(String nodeId, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.clusterCountFailureReports(nodeId, f));
    }

    @Override
    public RedisClient clusterCountkeysinslot(long slot, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.clusterCountkeysinslot(slot, f));
    }

    @Override
    public RedisClient clusterDelslots(long slot, Handler<AsyncResult<Void>> handler) {
        return common(handler, (Handler<AsyncResult<Void>> f) -> redisClient.clusterDelslots(slot, f));
    }

    @Override
    public RedisClient clusterDelslotsMany(List<Long> slots, Handler<AsyncResult<Void>> handler) {
        return common(handler, (Handler<AsyncResult<Void>> f) -> redisClient.clusterDelslotsMany(slots, f));
    }

    @Override
    public RedisClient clusterFailover(Handler<AsyncResult<Void>> handler) {
        return common(handler, (Handler<AsyncResult<Void>> f) -> redisClient.clusterFailover(f));
    }

    @Override
    public RedisClient clusterFailOverWithOptions(FailoverOptions options, Handler<AsyncResult<Void>> handler) {
        return common(handler, (Handler<AsyncResult<Void>> f) -> redisClient.clusterFailOverWithOptions(options, f));
    }

    @Override
    public RedisClient clusterForget(String nodeId, Handler<AsyncResult<Void>> handler) {
        return common(handler, (Handler<AsyncResult<Void>> f) -> redisClient.clusterForget(nodeId, f));
    }

    @Override
    public RedisClient clusterGetkeysinslot(long slot, long count, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.clusterGetkeysinslot(slot, count, f));
    }

    @Override
    public RedisClient clusterInfo(Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.clusterInfo(f));
    }

    @Override
    public RedisClient clusterKeyslot(String key, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.clusterKeyslot(key, f));
    }

    @Override
    public RedisClient clusterMeet(String ip, long port, Handler<AsyncResult<Void>> handler) {
        return common(handler, (Handler<AsyncResult<Void>> f) -> redisClient.clusterMeet(ip, port, f));
    }

    @Override
    public RedisClient clusterNodes(Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.clusterNodes(f));
    }

    @Override
    public RedisClient clusterReplicate(String nodeId, Handler<AsyncResult<Void>> handler) {
        return common(handler, (Handler<AsyncResult<Void>> f) -> redisClient.clusterReplicate(nodeId, f));
    }

    @Override
    public RedisClient clusterReset(Handler<AsyncResult<Void>> handler) {
        return common(handler, (Handler<AsyncResult<Void>> f) -> redisClient.clusterReset(f));
    }

    @Override
    public RedisClient clusterResetWithOptions(ResetOptions options, Handler<AsyncResult<Void>> handler) {
        return common(handler, (Handler<AsyncResult<Void>> f) -> redisClient.clusterResetWithOptions(options, f));
    }

    @Override
    public RedisClient clusterSaveconfig(Handler<AsyncResult<Void>> handler) {
        return common(handler, (Handler<AsyncResult<Void>> f) -> redisClient.clusterSaveconfig(f));
    }

    @Override
    public RedisClient clusterSetConfigEpoch(long epoch, Handler<AsyncResult<Void>> handler) {
        return common(handler, (Handler<AsyncResult<Void>> f) -> redisClient.clusterSetConfigEpoch(epoch, f));
    }

    @Override
    public RedisClient clusterSetslot(long slot, SlotCmd subcommand, Handler<AsyncResult<Void>> handler) {
        return common(handler, (Handler<AsyncResult<Void>> f) -> redisClient.clusterSetslot(slot, subcommand, f));
    }

    @Override
    public RedisClient clusterSetslotWithNode(long slot, SlotCmd subcommand, String nodeId, Handler<AsyncResult<Void>> handler) {
        return common(handler, (Handler<AsyncResult<Void>> f) -> redisClient.clusterSetslotWithNode(slot, subcommand, nodeId, f));
    }

    @Override
    public RedisClient clusterSlaves(String nodeId, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.clusterSlaves(nodeId, f));
    }

    @Override
    public RedisClient clusterSlots(Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.clusterSlots(f));
    }

    @Override
    public RedisClient command(Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.command(f));
    }

    @Override
    public RedisClient commandCount(Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.commandCount(f));
    }

    @Override
    public RedisClient commandGetkeys(Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.commandGetkeys(f));
    }

    @Override
    public RedisClient commandInfo(List<String> commands, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.commandInfo(commands, f));
    }

    @Override
    public RedisClient configGet(String parameter, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.configGet(parameter, f));
    }

    @Override
    public RedisClient configRewrite(Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.configRewrite(f));
    }

    @Override
    public RedisClient configSet(String parameter, String value, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.configSet(parameter, value, f));
    }

    @Override
    public RedisClient configResetstat(Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.configResetstat(f));
    }

    @Override
    public RedisClient dbsize(Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.dbsize(f));
    }

    @Override
    public RedisClient debugObject(String key, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.debugObject(key, f));
    }

    @Override
    public RedisClient debugSegfault(Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.debugSegfault(f));
    }

    @Override
    public RedisClient decr(String key, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.decr(key, f));
    }

    @Override
    public RedisClient decrby(String key, long decrement, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.decrby(key, decrement, f));
    }

    @Override
    public RedisClient del(String key, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.del(key, f));
    }

    @Override
    public RedisClient delMany(List<String> keys, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.delMany(keys, f));
    }

    @Override
    public RedisClient dump(String key, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.dump(key, f));
    }

    @Override
    public RedisClient echo(String message, Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.echo(message, f));
    }

    @Override
    public RedisClient eval(String script, List<String> keys, List<String> args, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.eval(script, keys, args, f));
    }

    @Override
    public RedisClient evalsha(String sha1, List<String> keys, List<String> values, Handler<AsyncResult<JsonArray>> handler) {
        return common(handler, (Handler<AsyncResult<JsonArray>> f) -> redisClient.evalsha(sha1, keys, values, f));
    }

    @Override
    public RedisClient exists(String key, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.exists(key, f));
    }

    @Override
    public RedisClient expire(String key, long seconds, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.expire(key, seconds, f));
    }

    @Override
    public RedisClient expireat(String key, long seconds, Handler<AsyncResult<Long>> handler) {
        return common(handler, (Handler<AsyncResult<Long>> f) -> redisClient.expireat(key, seconds, f));
    }

    @Override
    public RedisClient flushall(Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.flushall(f));
    }

    @Override
    public RedisClient flushdb(Handler<AsyncResult<String>> handler) {
        return common(handler, (Handler<AsyncResult<String>> f) -> redisClient.flushdb(f));
    }


    public static void main(String[] args) throws FileNotFoundException {
        List<String> lines = new ArrayList<>();


        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("D:/1.txt")));

        lines = reader.lines().collect(Collectors.toList());

        Pattern pattern = Pattern.compile("public RedisClient ([a-zA-Z]+)\\((.+)\\) \\{");
        Pattern patternType = Pattern.compile("Handler<AsyncResult<([a-zA-Z]+)>> handler");
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (line.trim().equals("@Override")) {
                String methodLine = lines.get(i + 1);
                Matcher matcher = pattern.matcher(methodLine);
                if (matcher.find()) {
                    StringBuilder sb = new StringBuilder();

                    String methodName = matcher.group(1);
                    String paramsStr = matcher.group(2);
                    List<String> params = Arrays.stream(paramsStr.split(","))
                            .map(String::trim).collect(Collectors.toList());

                    String handlerParam = params.remove(params.size() - 1);

                    String type = null;

                    Matcher matcherType = patternType.matcher(handlerParam);

                    if (matcherType.find()) {
                        type = matcherType.group(1);
                    }
                    try {

                        params = params.stream().map(s -> {
                            int index = s.lastIndexOf(" ");
                            return s.substring(index, s.length());
                        }).collect(Collectors.toList());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    params.add("f");
                    sb.append("return ")
                            .append("common(handler, (Handler<AsyncResult<" + type + ">> f) -> redisClient.")
                            .append(methodName).append("(")
                            .append(params.stream().collect(Collectors.joining(", ")))
                            .append("));");

                    lines.set(i + 2, sb.toString());
                }
            }
        }

        lines.forEach(li -> System.out.println(li));
    }
}
