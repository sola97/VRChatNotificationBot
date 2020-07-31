package cn.sola97.vrchat.service.impl;

import cn.sola97.vrchat.entity.Moderation;
import cn.sola97.vrchat.entity.User;
import cn.sola97.vrchat.entity.UserOnline;
import cn.sola97.vrchat.entity.World;
import cn.sola97.vrchat.service.CacheService;
import cn.sola97.vrchat.utils.TimeUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class CacheServiceImpl implements CacheService {
    @Autowired
    TimeUtil timeUtil;
    @Autowired
    RedisTemplate redisTemplate;
    private Map<String, ZonedDateTime> offlineMap = null;
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
    @Value("${cache.nonFriend}")
    String nonFriendUserKey;
    @Value("${cache.online.expire}")
    int onlineExpire;
    @Value("${cache.nonFriend.expire}")
    int nonFriendExpire;
    @Value("${scheduled.checkOnline.period}")
    int checkOnlinePeriod;
    @Value("${vrchat.currentUser.moderated}")
    String playerModeratedKey;

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
    public void setNonFriendUser(User user) {
        set(nonFriendUserKey + user.getId(), user, nonFriendExpire);
    }

    @Override
    public UserOnline getOnlineUser(String id) {
        Object user = get(onlineUserKey + id);
        if (user != null) return (UserOnline) user;
        return null;
    }

    @Override
    public User getNonFriendUser(String id) {
        Object user = get(nonFriendUserKey + id);
        if (user != null) return (User) user;
        return null;
    }

    @Override
    public boolean setUserOffline(String id) {
        if (offlineMap == null) {
            offlineMap = new PassiveExpiringMap<>(checkOnlinePeriod + 1, TimeUnit.SECONDS);
        }
        boolean success = redisTemplate.expire(onlineUserKey + id, checkOnlinePeriod + 1, TimeUnit.SECONDS);
        //记录Event的下线时间
        if (success) offlineMap.put(id, timeUtil.getZonedDateTime());
        return success;
    }

    @Override
    public ZonedDateTime getUserOfflineTime(String id) {
        if (offlineMap == null) {
            offlineMap = new PassiveExpiringMap<>(checkOnlinePeriod + 1, TimeUnit.SECONDS);
        }
        return offlineMap.getOrDefault(id, timeUtil.getZonedDateTime().minusSeconds(onlineExpire));
    }

    @Override
    public List<Moderation> getPlayerModerated() {
        if (redisTemplate.hasKey(playerModeratedKey)) {
            List<Moderation> list = (List<Moderation>) redisTemplate.opsForValue().get(playerModeratedKey);
            return list;
        }
        return new ArrayList<>();
    }

    @Override
    public void setPlayerModerated(List<Moderation> moderations) {
        if (redisTemplate.hasKey(playerModeratedKey)) {
            redisTemplate.delete(playerModeratedKey);
        }
        redisTemplate.opsForValue().set(playerModeratedKey, moderations);
    }

    @Override
    public List<Moderation> diffPlayerModerated(List<Moderation> moderations) {
        List<Moderation> playerModerated = getPlayerModerated();
        if (playerModerated.isEmpty()) {
            setPlayerModerated(moderations);
            return playerModerated;
        }
        List<Moderation> subtract = (List<Moderation>) CollectionUtils.subtract(moderations, playerModerated);
        setPlayerModerated(moderations);
        return subtract;
    }
}
