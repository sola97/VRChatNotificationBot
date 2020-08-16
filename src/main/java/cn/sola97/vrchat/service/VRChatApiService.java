package cn.sola97.vrchat.service;

import cn.sola97.vrchat.entity.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface VRChatApiService {

    ResponseEntity<CurrentUser> auth(String username, String password);

    List<UserOnline> getFriends(Boolean offline);

    List<UserOnline> getFriendsWithCache(Boolean offline);

    User getUserById(String id, Boolean withCache);

    CurrentUser getCurrentUserDetails(Boolean withCache);

    World getWorldById(String id, Boolean withCache);

    List<Moderation> getPlayerModerated();

    List<User> searchUser(String name, int num, int offset);

    Integer getVisits();

    VRCResponse joinLocation(String instrance);

    List<User> getUserByNameOrId(String nameOrId, Boolean online);
}
