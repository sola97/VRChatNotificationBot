package cn.sola97.vrchat.service.impl;

import cn.sola97.vrchat.entity.*;
import cn.sola97.vrchat.service.CacheService;
import cn.sola97.vrchat.service.CookieService;
import cn.sola97.vrchat.service.VRChatApiService;
import cn.sola97.vrchat.utils.RetryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Component
public class VRChatApiServiceImpl implements VRChatApiService {
    Logger logger = LoggerFactory.getLogger(VRChatApiServiceImpl.class);
    @Resource
    private VRChatApiService vrchatApiServiceImpl;
    @Autowired
    private RestTemplate apiRestTemplate;
    @Autowired
    CookieService cookieServiceImpl;
    @Autowired
    CacheService cacheServiceImpl;
    @Autowired
    @Qualifier("asyncExecutor")
    Executor asyncExecutor;
    @Value("${cache.friends}")
    String friendsCacheKey;
    @Value("${cache.currentUser}")
    String currentUserCacheKey;

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 0))
    @Override
    public List<UserOnline> getFriends(Boolean offline) {
        int total = 0;
        if (offline) {
            total = vrchatApiServiceImpl.getCurrentUserDetails(true).getOfflineFriends().size();
        } else {
            total = vrchatApiServiceImpl.getCurrentUserDetails(true).getOnlineFriends().size();
        }
        List<UserOnline> userList = new ArrayList<>();
        List<CompletableFuture<User[]>> futures = new ArrayList<>();
        int n = 100;
        int offset = 0;
        while (offset < total + 10) {
            int finalOffset = offset;
            CompletableFuture<User[]> retry = RetryUtil.retry(() -> CompletableFuture.supplyAsync(() -> {
                String uri = "/auth/user/friends?offline=" + offline.toString().toLowerCase() + "&n=" + n + "&offset=" + finalOffset;
                return apiRestTemplate.getForObject(uri, User[].class);
            }, asyncExecutor), 5);
            futures.add(retry);
            offset += n;
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).exceptionally(ex -> {
            logger.error("error on getFriends:" + ex.getMessage());
            return null;
        }).join();

        Map<Boolean, List<CompletableFuture<User[]>>> result =
                futures.stream().collect(Collectors.partitioningBy(CompletableFuture::isCompletedExceptionally));
        for (CompletableFuture<User[]> completableFuture : result.get(Boolean.FALSE)) {
            try {
                User[] users = completableFuture.get();
                userList.addAll(Arrays.asList(users));
            } catch (Exception e) {
                logger.error("CompletableFuture<User[]> completableFuture error", e);
            }
        }
        for (UserOnline userOnline : userList) {
            userOnline.setFriendIndex(cookieServiceImpl.getCurrentUserFriendIndex(userOnline.getId()));
        }
        if (offline) {
            userList.forEach(user -> user.setState("offline"));
        } else {
            userList.forEach(user -> user.setState("online"));
        }
        logger.debug("查询到好友数量：" + userList.size() + "  offline:" + offline.toString());
        return userList;
    }

    @Override
    public List<UserOnline> getFriendsWithCache(Boolean offline) {
        String status = offline ? "offline" : "online";
        if (cacheServiceImpl.exsits(friendsCacheKey + ":online") && cacheServiceImpl.exsits(friendsCacheKey + ":offline")) {
            return (List<UserOnline>) cacheServiceImpl.get(friendsCacheKey + ":" + status);
        }
        try {
            CompletableFuture<List<UserOnline>> offlineUsers = CompletableFuture.supplyAsync(() -> vrchatApiServiceImpl.getFriends(true));
            CompletableFuture<List<UserOnline>> onlineUsers = CompletableFuture.supplyAsync(() -> vrchatApiServiceImpl.getFriends(false));
            offlineUsers.join();
            onlineUsers.join();
            if (!offlineUsers.isCompletedExceptionally() && !onlineUsers.isCompletedExceptionally()) {
                cacheServiceImpl.set(friendsCacheKey + ":online", onlineUsers.get());
                cacheServiceImpl.set(friendsCacheKey + ":offline", offlineUsers.get());
            }
        } catch (Exception e) {
            logger.error("getFriendsWithCache error", e);
        }
        return (List<UserOnline>) cacheServiceImpl.get(friendsCacheKey + ":" + status);
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 0))
    @Override
    public User getUserById(String id, Boolean withCache) {
        if (id.equals(cookieServiceImpl.getCurrentUserId())) {
            return vrchatApiServiceImpl.getCurrentUserDetails(false);
        }
        if (withCache) {
            User userCache = cacheServiceImpl.getUserCache(id);
            if (userCache != null) return userCache;
        }
        String uri = "/users/" + id;
        User user = apiRestTemplate.getForObject(uri, User.class);
        cacheServiceImpl.setUserCache(user);
        return user;
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 0))
    @Override
    public CurrentUser getCurrentUserDetails(Boolean withCache) {
        if (withCache) {
            Object user = cacheServiceImpl.get(currentUserCacheKey);
            if (user != null && ((CurrentUser) user).getId() != null) {
                return (CurrentUser) user;
            }
        }
        String uri = "/auth/user";
        CurrentUser currentUser = apiRestTemplate.getForObject(uri, CurrentUser.class);
        assert currentUser.getId() != null;
        cacheServiceImpl.set(currentUserCacheKey, currentUser);
        return currentUser;
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 0))
    @Override
    public World getWorldById(String id, Boolean withCache) {
        if (id == null) return null;
        if ("private".equals(id) || "offline".equals(id)) {
            World world = new World();
            world.setId(id);
            world.setName(id);
            world.setImageUrl("https://assets.vrchat.com/www/images/default_" + id + "_image.png");
            world.setThumbnailImageUrl("https://assets.vrchat.com/www/images/default_" + id + "_image.png");
            world.setInstances(new ArrayList<>());
            return world;
        }
        if (withCache) {
            World world = cacheServiceImpl.getWorldCache(id);
            if (world != null) return world;
        }
        String uri = "/worlds/" + id;
        World world = apiRestTemplate.getForObject(uri, World.class);
        cacheServiceImpl.setWorldCache(world);
        return world;
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 0))
    @Override
    public List<Moderation> getPlayerModerations() {
        String uri = "/auth/user/playermoderated";
        Moderation[] moderations = apiRestTemplate.getForObject(uri, Moderation[].class);
        assert moderations != null;
        return Arrays.asList(moderations);
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 0))
    @Override
    public List<User> searchUser(String name, int num, int offset) {
        String uri = UriComponentsBuilder.fromUriString("/users")
                .queryParam("search", name)
                .queryParam("n", num)
                .queryParam("offset", offset).build(false).toUriString();
        User[] users = apiRestTemplate.getForObject(uri, User[].class);
        if (users != null && users.length > 0) {
            return Arrays.asList(users);
        }
        return new ArrayList<>();
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 0))
    @Override
    public Integer getVisits() {
        String uri = "/visits";
        return apiRestTemplate.getForObject(uri, Integer.class);
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 0))
    @Override
    public List<User> getUserByDisplayName(String displayName) {
        if ("*".equals(displayName)) {
            User user = new User();
            user.setDisplayName("*");
            user.setId("*");
            return Collections.singletonList(user);
        }
        if (displayName.startsWith("usr_")) {
            User user = getUserById(displayName, false);
            return Collections.singletonList(user);
        }
        if (cookieServiceImpl.getCurrentUserName().matches("(?i:[\\s\\S]*" + displayName + "[\\s\\S]*)")) {
            return Collections.singletonList(getCurrentUserDetails(true));
        }
        List<UserOnline> onlines = vrchatApiServiceImpl.getFriendsWithCache(false);
        List<User> onlinesFiltered = onlines.stream().map(user -> (User) user).filter(user -> (user).getDisplayName().matches("(?i:[\\s\\S]*" + displayName + "[\\s\\S]*)")).collect(Collectors.toList());
        if (onlinesFiltered.size() > 0) {
            return onlinesFiltered;
        } else {
            List<UserOnline> all = vrchatApiServiceImpl.getFriendsWithCache(true);
            List<User> allFiltered = all.stream().map(user -> (User) user).filter(user -> user.getDisplayName().matches("(?i:[\\s\\S]*" + displayName + "[\\s\\S]*)")).collect(Collectors.toList());
            return allFiltered;
        }
    }
}
