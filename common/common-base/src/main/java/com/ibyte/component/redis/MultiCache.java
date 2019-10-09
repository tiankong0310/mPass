package com.ibyte.component.redis;

import com.ibyte.common.util.thread.ThreadLocalUtil;
import lombok.Getter;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Description: <多重缓存，在Redis的基础上，增加了一个本地线程的缓存，减少远程访问>
 *
 * @author li.Shangzhi
 * @Date: 2019-10-09 22:21
 */
@Component
public class MultiCache {

    private static final String KEY = MultiCache.class.getName();

    protected static final Object NULL = new Object();

    @Getter
    @Autowired
    private RedissonClient redisson;

    private Map<String, Object> getThreadLocal() {
        Map<String, Object> map = null;
        if (ThreadLocalUtil.isAvailable()) {
            map = ThreadLocalUtil.getLocalVar(KEY);
            if (map == null) {
                map = new HashMap<>(16);
                ThreadLocalUtil.setLocalVar(KEY, map);
            }
        }
        return map;
    }

    /** 读 */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        Object value = null;
        Map<String, Object> map = getThreadLocal();
        if (map != null) {
            value = map.get(key);
            if (value != null) {
                return value == NULL ? null : (T) value;
            }
        }
        value = redisson.getBucket(key, JsonJacksonCodec.INSTANCE).get();
        if (map != null) {
            map.put(key, value == null ? NULL : value);
        }
        return (T) value;
    }

    /** 写 */
    public void set(String key, Object value) {
        Map<String, Object> map = getThreadLocal();
        if (map != null) {
            map.put(key, value);
        }
        redisson.getBucket(key, JsonJacksonCodec.INSTANCE).set(value);
    }

    /** 写 */
    public void set(String key, Object value, long timeToLive,
                    TimeUnit timeUnit) {
        Map<String, Object> map = getThreadLocal();
        if (map != null) {
            map.put(key, value);
        }
        redisson.getBucket(key, JsonJacksonCodec.INSTANCE).set(value,
                timeToLive, timeUnit);
    }

    /** 写（当无值时） */
    public boolean trySet(String key, Object value, long timeToLive,
                          TimeUnit timeUnit) {
        boolean success = redisson.getBucket(key, JsonJacksonCodec.INSTANCE)
                .trySet(value, timeToLive, timeUnit);
        Map<String, Object> map = getThreadLocal();
        if (map != null) {
            if (success) {
                map.put(key, value);
            } else {
                map.remove(key);
            }
        }
        return success;
    }

    /** 删 */
    public boolean remove(String key) {
        Map<String, Object> map = getThreadLocal();
        if (map != null) {
            map.put(key, NULL);
        }
        return redisson.getBucket(key, JsonJacksonCodec.INSTANCE).delete();
    }

    /** 读（Hash） */
    @SuppressWarnings("unchecked")
    public <T> T hget(String key, String field) {
        Object map = null;
        Object value = null;
        /**
         * CACHE:key==NULL 表示key不存在<br>
         * CACHE:key==MAP 表示key存在
         */
        // 从线程中读
        Map<String, Object> cache = getThreadLocal();
        if (cache != null) {
            map = cache.get(key);
            if (map != null) {
                if (map == NULL) {
                    // key不存在
                    return null;
                }
                value = ((Map<String, Object>) map).get(field);
                if (value != null) {
                    return value == NULL ? null : (T) value;
                }
            }
        }
        // 从redis读并回写线程
        RMap<String, Object> rmap = redisson.getMap(key,
                JsonJacksonCodec.INSTANCE);
        boolean exists = map != null || rmap.isExists();
        if (exists) {
            value = rmap.get(field);
        }
        if (cache != null) {
            if (exists) {
                if (map == null) {
                    map = new HashMap<>(16);
                    cache.put(key, map);
                }
                ((Map<String, Object>) map).put(field,
                        value == null ? NULL : value);
            } else {
                cache.put(key, NULL);
            }
        }
        return (T) value;
    }

    /** 读（Hash） */
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> hget(String key) {
        Object map = null;
        /**
         * CACHE:key==NULL 表示key不存在<br>
         * CACHE:key==MAP 表示key存在
         */
        // 从线程中读
        Map<String, Object> cache = getThreadLocal();
        if (cache != null) {
            map = cache.get(key);
            if (map != null) {
                if (map == NULL) {
                    // key不存在
                    return null;
                }
            }
        }
        // 从redis读并回写线程
        RMap<String, Object> rmap = redisson.getMap(key,
                JsonJacksonCodec.INSTANCE);
        boolean exists = map != null || rmap.isExists();
        if (exists) {
            map = new HashMap<>(rmap);
        }
        if (cache != null) {
            cache.put(key, map == null ? NULL : map);
        }
        return (Map<String, T>) map;
    }

    /** key是否存在（Hash） */
    public boolean hIsExists(String key) {
        Object map = null;
        /**
         * CACHE:key==NULL 表示key不存在<br>
         * CACHE:key==MAP 表示key存在
         */
        // 从线程中读
        Map<String, Object> cache = getThreadLocal();
        if (cache != null) {
            map = cache.get(key);
            if (map != null) {
                return map != NULL;
            }
        }
        // 从redis读并回写线程
        RMap<String, Object> rmap = redisson.getMap(key,
                JsonJacksonCodec.INSTANCE);
        boolean exists = rmap.isExists();
        if (cache != null) {
            if (exists) {
                cache.put(key, new HashMap<>(16));
            } else {
                cache.put(key, NULL);
            }
        }
        return exists;
    }

    /** 写 */
    public void hset(String key, Map<String, ?> map, long timeToLive,
                     TimeUnit timeUnit) {
        Map<String, Object> cache = getThreadLocal();
        if (cache != null) {
            cache.put(key, map);
        }
        RMap<String, Object> rmap = redisson.getMap(key,
                JsonJacksonCodec.INSTANCE);
        rmap.clear();
        rmap.putAll(map);
        rmap.expire(timeToLive, timeUnit);
    }

    /** 写，当key不存在时无效，并返回false */
    @SuppressWarnings("unchecked")
    public boolean hset(String key, String field, Object value) {
        Object map = null;
        /**
         * CACHE:key==NULL 表示key不存在<br>
         * CACHE:key==MAP 表示key存在
         */
        // 从线程中判断key是否存在
        Map<String, Object> cache = getThreadLocal();
        if (cache != null) {
            map = cache.get(key);
            if (map == NULL) {
                // key不存在，写失败
                return false;
            }
        }
        // 从redis读并回写线程
        RMap<String, Object> rmap = redisson.getMap(key,
                JsonJacksonCodec.INSTANCE);
        boolean exists = map != null || rmap.isExists();
        if (!exists) {
            if (cache != null) {
                cache.put(key, NULL);
            }
            return false;
        }
        // 写入数据
        rmap.put(field, value);
        if (cache != null) {
            if (map == null) {
                map = new HashMap<>(16);
                cache.put(key, map);
            }
            ((Map<String, Object>) map).put(field,
                    value == null ? NULL : value);
        }
        return true;
    }

}
