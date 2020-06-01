package pers.snapped.redis;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * ▓██   ██▓ ▒█████   ▄▄▄       ██ ▄█▀▓█████
 * ▒██  ██▒▒██▒  ██▒▒████▄     ██▄█▒ ▓█   ▀
 * ▒██ ██░▒██░  ██▒▒██  ▀█▄  ▓███▄░ ▒███
 * ░ ▐██▓░▒██   ██░░██▄▄▄▄██ ▓██ █▄ ▒▓█  ▄
 * ░ ██▒▓░░ ████▓▒░ ▓█   ▓██▒▒██▒ █▄░▒████▒
 * ██▒▒▒ ░ ▒░▒░▒░  ▒▒   ▓▒█░▒ ▒▒ ▓▒░░ ▒░ ░
 * ▓██ ░▒░   ░ ▒ ▒░   ▒   ▒▒ ░░ ░▒ ▒░ ░ ░  ░
 * ▒ ▒ ░░  ░ ░ ░ ▒    ░   ▒   ░ ░░ ░    ░
 * ░ ░         ░ ░        ░  ░░  ░      ░  ░
 * ░ ░
 */
@Component
public class RedisService {

    @Autowired
    private JedisPool jedisPool;

    public <T> T get(KeyPrefix prefix, String key, Class<T> tClass) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            String str = jedis.get(realKey);
            return stringToBean(str, tClass);
        } finally {
            close(jedis);
        }

    }


    public <T> boolean set(KeyPrefix prefix, String key, T value) {
        Jedis jedis = null;
        try {
            String str = beanTOString(value);
            if (str == null) {
                return false;
            }
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            if (prefix.expireSecond() > 0) {
                jedis.setex(realKey, prefix.expireSecond(), str);
            } else {
                jedis.set(realKey, str);
            }
            return true;
        } finally {
            close(jedis);
        }
    }

    public long decr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            return jedis.decr(realKey);
        } finally {
            close(jedis);
        }
    }

    public <T> boolean exists(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis =  jedisPool.getResource();
            //生成真正的key
            String realKey  = prefix.getPrefix() + key;
            return  jedis.exists(realKey);
        }finally {
            close(jedis);
        }
    }

    public static <T> T stringToBean(String str, Class<T> tClass) {
        if (str == null || str.length() <= 0 || tClass == null) {
            return null;
        }
        if (tClass == int.class || tClass == Integer.class) {
            return (T) Integer.valueOf(str);
        } else if (tClass == long.class || tClass == Long.class) {
            return (T) Long.valueOf(str);
        } else if (tClass == String.class) {
            return (T) str;
        } else {
            return JSON.toJavaObject(JSON.parseObject(str), tClass);
        }
    }

    public static <T> String beanTOString(T value) {
        if (value == null) {
            return null;
        }
        Class<?> aClass = value.getClass();
        if (aClass == int.class || aClass == Integer.class) {
            return value + "";
        } else if (aClass == long.class || aClass == Long.class) {
            return value + "";
        } else if (aClass == String.class) {
            return (String) value;
        } else {
            return JSON.toJSONString(value);
        }
    }

    private void close(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

}
