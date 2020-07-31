package cn.sola97.vrchat.service;

import cn.sola97.vrchat.entity.Moderation;
import cn.sola97.vrchat.entity.User;
import cn.sola97.vrchat.entity.UserOnline;
import cn.sola97.vrchat.entity.World;

import java.time.ZonedDateTime;
import java.util.List;

public interface CacheService {
    Object get(String key);

    void set(String key, Object object);

    void set(String key, Object object, int seconds);

    boolean exsits(String key);

    World getWorldCache(String id);

    void setWorldCache(World world);

    User getUserCache(String id);

    void setUserCache(User user);

    void setUserOnline(UserOnline user);

    void setNonFriendUser(User user);

    UserOnline getOnlineUser(String id);

    User getNonFriendUser(String id);

    boolean setUserOffline(String id);

    ZonedDateTime getUserOfflineTime(String id);

    List<Moderation> getPlayerModerated();

    void setPlayerModerated(List<Moderation> moderations);

    List<Moderation> diffPlayerModerated(List<Moderation> moderations);
}
