package cn.sola97.vrchat.pojo.impl;

import cn.sola97.vrchat.pojo.EventContent;

public class WsFriendContent extends EventContent {
    private String userId;
    private Boolean canRequestInvite;
    private String location;

    @Override
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean getCanRequestInvite() {
        return canRequestInvite;
    }

    public void setCanRequestInvite(Boolean canRequestInvite) {
        this.canRequestInvite = canRequestInvite;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "WsFriendContent{" +
                "userId='" + userId + '\'' +
                ", canRequestInvite=" + canRequestInvite +
                ", location='" + location + '\'' +
                "} " + super.toString();
    }
}