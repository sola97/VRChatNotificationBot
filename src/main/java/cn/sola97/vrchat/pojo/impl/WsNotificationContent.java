package cn.sola97.vrchat.pojo.impl;


import cn.sola97.vrchat.pojo.EventContent;

import java.util.Date;

public class WsNotificationContent extends EventContent {
    private String id;
    private String senderUserId;
    private String senderUsername;
    private String type;
    private String message;
    private Details details;
    private String seen;
    private Date created_at;
    private String receiverUserId;

    @Override
    public String getUserId() {
        return getSenderUserId();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(String senderUserId) {
        this.senderUserId = senderUserId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Details getDetails() {
        return details;
    }

    public void setDetails(Details details) {
        this.details = details;
    }

    public String getReceiverUserId() {
        return receiverUserId;
    }

    public void setReceiverUserId(String receiverUserId) {
        this.receiverUserId = receiverUserId;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public static class Details {
        private String worldId;
        private String worldName;
        private String platform;

        public Details() {
        }

        public String getWorldId() {
            return worldId;
        }

        public void setWorldId(String worldId) {
            this.worldId = worldId;
        }

        public String getWorldName() {
            return worldName;
        }

        public void setWorldName(String worldName) {
            this.worldName = worldName;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }
    }
}