package com.cherish.component.redis;

import com.cherish.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@ConditionalOnClass(StringRedisTemplate.class)
public class RedisManager {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public <T> T get(String key, Class<T> clazz){
        String resultStr = redisTemplate.opsForValue().get(key);
        if(resultStr == null) return null;
        return JsonUtils.toObject(resultStr, clazz);
    }

    public<K, V> Map<K, V> mget(String prefix, List<K> args, Class<V> clazz){
        List<String> keys = args.stream().map(arg -> String.format(prefix, arg)).collect(Collectors.toList());
        List<String> caches = redisTemplate.opsForValue().multiGet(keys);
        Map<K, V> res = new HashMap<>();
        for(int i=0; i <keys.size(); i++){
            if(caches.get(i) != null){
                res.put(args.get(i), JsonUtils.toObject(caches.get(i), clazz));
            }
        }
        return res;
    }

    public<K, V> void mset(String prefix, Map<K, V> objMap){
        Map<String, String> cacheMap = new HashMap<>();
        objMap.forEach((k, v) -> cacheMap.put(String.format(prefix, k), JsonUtils.toJson(v)));
        redisTemplate.opsForValue().multiSet(cacheMap);
    }

    public void set(String key, Object data){
        redisTemplate.opsForValue().set(key, JsonUtils.toJson(data));
    }

    public void del(String key){
        redisTemplate.delete(key);
    }

    public boolean zadd(String key, String value, double score){
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    public long zremove(String key, String value){
        return redisTemplate.opsForZSet().remove(key, value);
    }

    public Set<String> zrange(String key, double start, double end, int count){
        return redisTemplate.opsForZSet().rangeByScore(key, start, end, 0 ,count);
    }

    public List<String> srandommembers(String key, int size){
        return redisTemplate.opsForSet().randomMembers(key, size);
    }
}
