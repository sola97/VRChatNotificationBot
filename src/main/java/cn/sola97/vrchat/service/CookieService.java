package cn.sola97.vrchat.service;

import cn.sola97.vrchat.entity.CurrentUser;

import java.util.List;

public interface CookieService {

    void setCurrentUserFriendList(List<String> friends);

    Boolean deleteCookie();

    void setCookie(String cookies);

    void setCurrentUser(CurrentUser user);

    String getCurrentUserId();

    String getCurrentUserName();

    CurrentUser getCurrentUser();

    String getCookie();

    Boolean exitsCookie();

    String getAuthToken();

    Integer getCurrentUserFriendIndex(String usrId);
}
