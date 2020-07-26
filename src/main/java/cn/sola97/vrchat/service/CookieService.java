package cn.sola97.vrchat.service;

import cn.sola97.vrchat.entity.CurrentUser;

public interface CookieService {

    Boolean deleteCookie();

    void setCookie(String cookies);

    String getCurrentUserId();

    String getCurrentUserName();

    CurrentUser getCurrentUser();

    String getCookie();

    Boolean exitsCookie();

    String getAuthToken();

    Integer getCurrentUserFriendIndex(String usrId);
}
