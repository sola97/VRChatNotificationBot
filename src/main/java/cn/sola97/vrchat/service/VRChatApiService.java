package cn.sola97.vrchat.service;

import cn.sola97.vrchat.entity.*;

import java.util.List;

public interface VRChatApiService {

    List<UserOnline> getFriends(Boolean offline);

    List<UserOnline> getFriendsWithCache(Boolean offline);

    User getUserById(String id, Boolean withCache);

    CurrentUser getCurrentUserDetails(Boolean withCache);

    World getWorldById(String id, Boolean withCache);

    List<Moderation> getPlayerModerations();

    List<User> searchUser(String name, int num, int offset);

    Integer getVisits();

    List<User> getUserByDisplayName(String displayName);
}
