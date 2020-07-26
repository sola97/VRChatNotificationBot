package cn.sola97.vrchat.entity;

import java.util.Date;
import java.util.List;

public interface UserOnline {


    String getId();

    void setId(String id);

    String getUsername();

    void setUsername(String username);

    String getDisplayName();

    void setDisplayName(String displayName);

    String getCurrentAvatarImageUrl();

    void setCurrentAvatarImageUrl(String currentAvatarImageUrl);

    String getCurrentAvatarThumbnailImageUrl();

    void setCurrentAvatarThumbnailImageUrl(String currentAvatarThumbnailImageUrl);

    String getStatus();

    void setStatus(String status);

    String getStatusDescription();

    void setStatusDescription(String statusDescription);

    String getState();

    void setState(String state);

    List<String> getTags();

    void setTags(List<String> tags);

    Date getLast_login();

    void setLast_login(Date last_login);

    String getLast_platform();

    void setLast_platform(String last_platform);

    Boolean getFriend();

    void setFriend(Boolean friend);

    String getFriendKey();

    void setFriendKey(String friendKey);

    String getLocation();

    void setLocation(String location);

    Integer getFriendIndex();

    void setFriendIndex(Integer friendIndex);
}
