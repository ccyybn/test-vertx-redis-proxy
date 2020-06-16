package com.xxx.redisproxy;

public class RedisCMD {
    public OP op;
    public String key;
    public String field;
    public String val;

    public static RedisCMD buildDEL(String key) {
        return build(OP.DEL, key, null, null);
    }

    public static RedisCMD buildLPUSH(String key, String val) {
        return build(OP.LPUSH, key, null, val);
    }

    public static RedisCMD buildRPUSH(String key, String val) {
        return build(OP.RPUSH, key, null, val);
    }

    public static RedisCMD buildHDEL(String key, String filed) {
        return build(OP.HDEL, key, filed, null);
    }

    public static RedisCMD buildHSET(String key, String filed, String val) {
        return build(OP.HSET, key, filed, val);
    }

    public static RedisCMD build(OP op, String key, String filed, String val) {
        RedisCMD cmd = new RedisCMD();
        cmd.op = op;
        cmd.key = key;
        cmd.field = filed;
        cmd.val = val;
        return cmd;
    }

    public static enum OP {
        HSET,
        HDEL,
        RPUSH,
        LPUSH,
        DEL
    }
}
