package cn.sola97.vrchat.service.impl;

import cn.sola97.vrchat.entity.User;
import cn.sola97.vrchat.entity.UserOnline;
import cn.sola97.vrchat.entity.World;
import cn.sola97.vrchat.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CacheServiceImpl implements CacheService {
    @Autowired
    RedisTemplate redisTemplate;
    @Value("${cache.friends}")
    String friendsCacheKey;
    @Value("${cache.currentUser}")
    String currentUserCacheKey;
    @Value("${cache.expire}")
    int seconds;
    @Value("${cache.world}")
    String worldCacheKey;
    @Value("${cache.world.expire}")
    int worldExpire;
    @Value("${cache.user}")
    String userCacheKey;
    @Value("${cache.user.expire}")
    int userExpire;
    @Value("${cache.online}")
    String onlineUserKey;
    @Value("${cache.online.expire}")
    int onlineExpire;
    @Override
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void set(String key, Object object) {
        redisTemplate.opsForValue().set(key, object);
        redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

    @Override
    public void set(String key, Object object, int seconds) {
        redisTemplate.opsForValue().set(key, object);
        redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean exsits(String key) {
        return redisTemplate.hasKey(key);
    }

    @Override
    public void clearCache() {
        redisTemplate.delete(friendsCacheKey);
        redisTemplate.delete(currentUserCacheKey);
    }

    @Override
    public World getWorldCache(String id) {
        Object world = get(worldCacheKey + id);
        if (world != null) return (World) world;
        return null;
    }

    @Override
    public void setWorldCache(World world) {
        this.set(worldCacheKey + world.getId(), world, worldExpire);
    }

    @Override
    public User getUserCache(String id) {
        Object user = get(userCacheKey + id);
        if (user != null) return (User) user;
        return null;
    }

    @Override
    public void setUserCache(User user) {
        this.set(userCacheKey + user.getId(), user, worldExpire);
    }

    @Override
    public void setUserOnline(UserOnline user) {
        set(onlineUserKey + user.getId(), user, onlineExpire);
    }

    @Override
    public UserOnline getOnlineUser(String id) {
        Object user = get(onlineUserKey + id);
        if (user != null) return (UserOnline) user;
        return null;
    }

    @Override
    public void setUserOffline(String id) {
        redisTemplate.delete(onlineUserKey + id);
    }
}
