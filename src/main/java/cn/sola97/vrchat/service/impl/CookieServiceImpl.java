package cn.sola97.vrchat.service.impl;

import cn.sola97.vrchat.entity.CurrentUser;
import cn.sola97.vrchat.service.CookieService;
import cn.sola97.vrchat.utils.CookieUtil;
import cn.sola97.vrchat.utils.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.Proxy;
import java.util.*;

@Service
public class CookieServiceImpl implements CookieService {
    private static final Logger logger = LoggerFactory.getLogger(CookieServiceImpl.class);
    @Value("${vrchat.currentUserFriends}")
    String currentUserFriendsKey;
    @Value("${vrchat.cookieKey}")
    private String cookieKey;
    @Value("${vrchat.username}")
    private String username;
    @Value("${vrchat.password}")
    private String password;
    @Value("${vrchat.rootUri}")
    String uri;
    @Value("${vrchat.currentUserId}")
    String currentUserIdKey;
    @Value("${vrchat.currentUser}")
    String currentUserKey;
    @Value("${vrchat.currentUserName}")
    String currentUserNameKey;
    @Value("${vrchat.api.proxy:}")
    String proxyString;
    @Autowired
    RedisTemplate redisTemplate;
    private Map<String, Integer> friendsIndexMap;

    private String authenticate() {
        Proxy proxy = HttpUtil.getProxy(proxyString);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36");
        headers.setBasicAuth(username, password);
        String URL = uri + "/auth/user?apiKey=JlE5Jldo5Jibnk5O5hTx6XVqsJu4WJ26";

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
        RestTemplate restTemplate = new RestTemplate();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        if (proxy != null)
            requestFactory.setProxy(proxy);
        restTemplate.setMessageConverters(Collections.singletonList(converter));
        restTemplate.setRequestFactory(requestFactory);

        ResponseEntity<CurrentUser> response = restTemplate.exchange(URL, HttpMethod.GET, new HttpEntity<>(headers), CurrentUser.class);
        String cookies = CookieUtil.processHeaders(response.getHeaders());
        setCookie(cookies);
        CurrentUser currentUser = response.getBody();
        setCurrentUserName(currentUser.getDisplayName());
        setCurrentUserId(currentUser.getId());
        setCurrentUser(currentUser);
        setCurrentUserFriendList(currentUser.getFriends());
        friendsIndexMap = convertFriendListToMap(currentUser.getFriends());
        return cookies;
    }

    private void setCurrentUserFriendList(List<String> friends) {
        logger.info("更新用户好友列表");
        try {
            redisTemplate.opsForList().rightPushAll(currentUserFriendsKey, friends);
        } catch (Exception e) {
            logger.error("setCurrentUserFriendList", e);
        }
    }

    @Override
    public Boolean deleteCookie() {
        logger.info("删除Cookie");
        return deleteCurrentUser() && redisTemplate.delete(cookieKey);
    }

    @Override
    public String getCookie() {
        if (exitsCookie()) {
            return redisTemplate.opsForValue().get(cookieKey).toString();
        }
        return authenticate();
    }

    private void setCurrentUserId(String userId) {
        logger.info("更新存储的用户Id");
        redisTemplate.opsForValue().set(currentUserIdKey, userId);
    }

    private void setCurrentUser(CurrentUser user) {
        logger.info("更新存储的用户");
        redisTemplate.opsForValue().set(currentUserKey, user);
    }

    private void setCurrentUserName(String displayName) {
        logger.info("更新存储的用户名");
        redisTemplate.opsForValue().set(currentUserNameKey, displayName);
    }

    private Boolean deleteCurrentUser() {
        return redisTemplate.delete(Arrays.asList(currentUserIdKey, currentUserKey, currentUserNameKey)) > 0;
    }

    @Override
    public String getCurrentUserId() {
        if (redisTemplate.hasKey(currentUserIdKey))
            return redisTemplate.opsForValue().get(currentUserIdKey).toString();
        authenticate();
        return getCurrentUserId();
    }

    @Override
    public String getCurrentUserName() {
        if (redisTemplate.hasKey(currentUserNameKey))
            return redisTemplate.opsForValue().get(currentUserNameKey).toString();
        authenticate();
        return getCurrentUserName();
    }

    @Override
    public CurrentUser getCurrentUser() {
        if (redisTemplate.hasKey(currentUserKey))
            return (CurrentUser) redisTemplate.opsForValue().get(currentUserKey);
        authenticate();
        return getCurrentUser();
    }

    @Override
    public void setCookie(String cookies) {
        logger.info("更新Cookie");
        redisTemplate.opsForValue().set(cookieKey, cookies);
    }

    @Override
    public Boolean exitsCookie() {
        return redisTemplate.hasKey(cookieKey);
    }

    @Override
    public String getAuthToken() {
        return CookieUtil.convertStringToMap(getCookie()).get("auth");
    }

    @Override
    public Integer getCurrentUserFriendIndex(String usrId) {
        Integer index = null;
        if (friendsIndexMap != null && !friendsIndexMap.isEmpty()) {
            index = friendsIndexMap.getOrDefault(usrId, null);
        } else if (redisTemplate.hasKey(currentUserFriendsKey)) {
            List<String> friends = (List<String>) redisTemplate.opsForList().range(currentUserFriendsKey, 0, -1);
            logger.info("从缓存中查询到好友列表 size:", friends.size());
            friendsIndexMap = convertFriendListToMap(friends);
            index = friendsIndexMap.getOrDefault(usrId, null);
        }
        if (index != null) return index + 1;
        return null;
    }

    private Map<String, Integer> convertFriendListToMap(List<String> friends) {
        Map<String, Integer> tempMap = new HashMap<>();
        for (int i = 0; i < friends.size(); i++) {
            tempMap.put(friends.get(i), i);
        }
        return tempMap;
    }
}
