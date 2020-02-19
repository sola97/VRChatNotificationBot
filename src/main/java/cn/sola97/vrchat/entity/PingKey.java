package cn.sola97.vrchat.entity;

import java.io.Serializable;

/**
 * @author 
 */
public class PingKey implements Serializable {
    private String channelId;

    private String usrId;

    private String discordId;

    private static final long serialVersionUID = 1L;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getUsrId() {
        return usrId;
    }

    public void setUsrId(String usrId) {
        this.usrId = usrId;
    }

    public String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        PingKey other = (PingKey) that;
        return (this.getChannelId() == null ? other.getChannelId() == null : this.getChannelId().equals(other.getChannelId()))
            && (this.getUsrId() == null ? other.getUsrId() == null : this.getUsrId().equals(other.getUsrId()))
            && (this.getDiscordId() == null ? other.getDiscordId() == null : this.getDiscordId().equals(other.getDiscordId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getChannelId() == null) ? 0 : getChannelId().hashCode());
        result = prime * result + ((getUsrId() == null) ? 0 : getUsrId().hashCode());
        result = prime * result + ((getDiscordId() == null) ? 0 : getDiscordId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", channelId=").append(channelId);
        sb.append(", usrId=").append(usrId);
        sb.append(", discordId=").append(discordId);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}