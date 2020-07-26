package cn.sola97.vrchat.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrentUser extends User {
    private List<String> activeFriends;
    private Integer acceptedTOSVersion;
    private String currentAvatarAssetUrl;
    private String emailVerified;
    private Feature feature;
    private List<String> friendGroupNames;
    private List<String> friends;
    private Boolean hasBirthday;
    private Boolean hasEmail;
    private Boolean hasLoggedInFromClient;
    private Boolean hasPendingEmail;
    private String homeLocation;
    private String obfuscatedEmail;
    private String obfuscatedPendingEmail;
    private String oculusId;
    private List<String> offlineFriends;
    private List<String> onlineFriends;
    private List<PastDisplayName> pastDisplayNames;
    private Object steamDetails;
    private String steamId;
    private Boolean twoFactorAuthEnabled;
    private Boolean unsubscribe;
    private Integer friendIndex;

    @Override
    public String toString() {
        return "CurrentUser{" +
                "activeFriends=" + activeFriends +
                ", acceptedTOSVersion=" + acceptedTOSVersion +
                ", currentAvatarAssetUrl='" + currentAvatarAssetUrl + '\'' +
                ", emailVerified='" + emailVerified + '\'' +
                ", feature=" + feature +
                ", friendGroupNames=" + friendGroupNames +
                ", friends=" + friends +
                ", hasBirthday=" + hasBirthday +
                ", hasEmail=" + hasEmail +
                ", hasLoggedInFromClient=" + hasLoggedInFromClient +
                ", hasPendingEmail=" + hasPendingEmail +
                ", homeLocation='" + homeLocation + '\'' +
                ", obfuscatedEmail='" + obfuscatedEmail + '\'' +
                ", obfuscatedPendingEmail='" + obfuscatedPendingEmail + '\'' +
                ", oculusId='" + oculusId + '\'' +
                ", offlineFriends=" + offlineFriends +
                ", onlineFriends=" + onlineFriends +
                ", pastDisplayNames=" + pastDisplayNames +
                ", steamDetails=" + steamDetails +
                ", steamId='" + steamId + '\'' +
                ", twoFactorAuthEnabled=" + twoFactorAuthEnabled +
                ", unsubscribe=" + unsubscribe +
                ", friendIndex=" + friendIndex +
                "} " + super.toString();
    }

    public List<String> getActiveFriends() {
        return activeFriends;
    }

    public void setActiveFriends(List<String> activeFriends) {
        this.activeFriends = activeFriends;
    }

    public Integer getAcceptedTOSVersion() {
        return acceptedTOSVersion;
    }

    public void setAcceptedTOSVersion(Integer acceptedTOSVersion) {
        this.acceptedTOSVersion = acceptedTOSVersion;
    }

    public String getCurrentAvatarAssetUrl() {
        return currentAvatarAssetUrl;
    }

    public void setCurrentAvatarAssetUrl(String currentAvatarAssetUrl) {
        this.currentAvatarAssetUrl = currentAvatarAssetUrl;
    }

    public String getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(String emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public List<String> getFriendGroupNames() {
        return friendGroupNames;
    }

    public void setFriendGroupNames(List<String> friendGroupNames) {
        this.friendGroupNames = friendGroupNames;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public Boolean getHasBirthday() {
        return hasBirthday;
    }

    public void setHasBirthday(Boolean hasBirthday) {
        this.hasBirthday = hasBirthday;
    }

    public Boolean getHasEmail() {
        return hasEmail;
    }

    public void setHasEmail(Boolean hasEmail) {
        this.hasEmail = hasEmail;
    }

    public Boolean getHasLoggedInFromClient() {
        return hasLoggedInFromClient;
    }

    public void setHasLoggedInFromClient(Boolean hasLoggedInFromClient) {
        this.hasLoggedInFromClient = hasLoggedInFromClient;
    }

    public Boolean getHasPendingEmail() {
        return hasPendingEmail;
    }

    public void setHasPendingEmail(Boolean hasPendingEmail) {
        this.hasPendingEmail = hasPendingEmail;
    }

    public String getHomeLocation() {
        return homeLocation;
    }

    public void setHomeLocation(String homeLocation) {
        this.homeLocation = homeLocation;
    }

    public String getObfuscatedEmail() {
        return obfuscatedEmail;
    }

    public void setObfuscatedEmail(String obfuscatedEmail) {
        this.obfuscatedEmail = obfuscatedEmail;
    }

    public String getObfuscatedPendingEmail() {
        return obfuscatedPendingEmail;
    }

    public void setObfuscatedPendingEmail(String obfuscatedPendingEmail) {
        this.obfuscatedPendingEmail = obfuscatedPendingEmail;
    }

    public String getOculusId() {
        return oculusId;
    }

    public void setOculusId(String oculusId) {
        this.oculusId = oculusId;
    }

    public List<String> getOfflineFriends() {
        return offlineFriends;
    }

    public void setOfflineFriends(List<String> offlineFriends) {
        this.offlineFriends = offlineFriends;
    }

    public List<String> getOnlineFriends() {
        return onlineFriends;
    }

    public void setOnlineFriends(List<String> onlineFriends) {
        this.onlineFriends = onlineFriends;
    }

    public List<PastDisplayName> getPastDisplayNames() {
        return pastDisplayNames;
    }

    public void setPastDisplayNames(List<PastDisplayName> pastDisplayNames) {
        this.pastDisplayNames = pastDisplayNames;
    }

    public Object getSteamDetails() {
        return steamDetails;
    }

    public void setSteamDetails(Object steamDetails) {
        this.steamDetails = steamDetails;
    }

    public String getSteamId() {
        return steamId;
    }

    public void setSteamId(String steamId) {
        this.steamId = steamId;
    }

    public Boolean getTwoFactorAuthEnabled() {
        return twoFactorAuthEnabled;
    }

    public void setTwoFactorAuthEnabled(Boolean twoFactorAuthEnabled) {
        this.twoFactorAuthEnabled = twoFactorAuthEnabled;
    }

    public Boolean getUnsubscribe() {
        return unsubscribe;
    }

    public void setUnsubscribe(Boolean unsubscribe) {
        this.unsubscribe = unsubscribe;
    }

    public Integer getFriendIndex() {
        return null;
    }

    public void setFriendIndex(Integer friendIndex) {
    }
}
class Feature{
    private Boolean twoFactorAuth;

    public Boolean getTwoFactorAuth() {
        return twoFactorAuth;
    }

    public void setTwoFactorAuth(Boolean twoFactorAuth) {
        this.twoFactorAuth = twoFactorAuth;
    }

    @Override
    public String toString() {
        return "Feature{" +
                "twoFactorAuth=" + twoFactorAuth +
                '}';
    }
}
class PastDisplayName{
    private String displayName;
    @JsonProperty("updated_at")
    private Date updatedAt;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "PastDisplayName{" +
                "displayName='" + displayName + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}