package cn.sola97.vrchat.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements UserOnline {
    private String id;
    private String username;
    private String displayName;
    private String bio;
    private List<String> bioLinks;
    private String currentAvatarImageUrl;
    private String currentAvatarThumbnailImageUrl;
    private String status;
    private String statusDescription;
    private String state;
    private List<String> tags;
    private String developerType;
    private Date last_login;
    private String last_platform;
    private Boolean allowAvatarCopying;
    private Boolean isFriend;
    private String friendKey;
    private String location;
    private String worldId;
    private String instanceId;

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", displayName='" + displayName + '\'' +
                ", bio=" + bio +
                ", bioLinks='" + bioLinks + '\'' +
                ", currentAvatarImageUrl='" + currentAvatarImageUrl + '\'' +
                ", currentAvatarThumbnailImageUrl='" + currentAvatarThumbnailImageUrl + '\'' +
                ", status='" + status + '\'' +
                ", statusDescription='" + statusDescription + '\'' +
                ", state='" + state + '\'' +
                ", tags='" + tags + '\'' +
                ", developerType='" + developerType + '\'' +
                ", last_login=" + last_login +
                ", last_platform='" + last_platform + '\'' +
                ", allowAvatarCopying=" + allowAvatarCopying +
                ", isFriend=" + isFriend +
                ", friendKey='" + friendKey + '\'' +
                ", location='" + location + '\'' +
                ", worldId='" + worldId + '\'' +
                ", instanceId='" + instanceId + '\'' +
                '}';
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<String> getBioLinks() {
        return bioLinks;
    }

    public void setBioLinks(List<String> bioLinks) {
        this.bioLinks = bioLinks;
    }

    @Override
    public String getCurrentAvatarImageUrl() {
        return currentAvatarImageUrl;
    }

    @Override
    public void setCurrentAvatarImageUrl(String currentAvatarImageUrl) {
        this.currentAvatarImageUrl = currentAvatarImageUrl;
    }

    @Override
    public String getCurrentAvatarThumbnailImageUrl() {
        return currentAvatarThumbnailImageUrl;
    }

    @Override
    public void setCurrentAvatarThumbnailImageUrl(String currentAvatarThumbnailImageUrl) {
        this.currentAvatarThumbnailImageUrl = currentAvatarThumbnailImageUrl;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String getStatusDescription() {
        return statusDescription;
    }

    @Override
    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public void setState(String state) {
        this.state = state;
    }

    @Override
    public List<String> getTags() {
        return tags;
    }

    @Override
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getDeveloperType() {
        return developerType;
    }

    public void setDeveloperType(String developerType) {
        this.developerType = developerType;
    }

    @Override
    public Date getLast_login() {
        return last_login;
    }

    @Override
    public void setLast_login(Date last_login) {
        this.last_login = last_login;
    }

    @Override
    public String getLast_platform() {
        return last_platform;
    }

    @Override
    public void setLast_platform(String last_platform) {
        this.last_platform = last_platform;
    }

    public Boolean getAllowAvatarCopying() {
        return allowAvatarCopying;
    }

    public void setAllowAvatarCopying(Boolean allowAvatarCopying) {
        this.allowAvatarCopying = allowAvatarCopying;
    }

    @Override
    public Boolean getFriend() {
        return isFriend;
    }

    @Override
    public void setFriend(Boolean friend) {
        isFriend = friend;
    }

    @Override
    public String getFriendKey() {
        return friendKey;
    }

    @Override
    public void setFriendKey(String friendKey) {
        this.friendKey = friendKey;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public void setLocation(String location) {
        this.location = location;
    }

    public String getWorldId() {
        return worldId;
    }

    public void setWorldId(String worldId) {
        this.worldId = worldId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
}