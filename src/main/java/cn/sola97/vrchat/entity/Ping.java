package cn.sola97.vrchat.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 
 */
public class Ping extends PingKey implements Serializable {
    private String discordName;

    private Byte mask;

    private Boolean disabled;

    private Date createdAt;

    private Date updatedAt;

    private static final long serialVersionUID = 1L;

    public String getDiscordName() {
        return discordName;
    }

    public void setDiscordName(String discordName) {
        this.discordName = discordName;
    }

    public Byte getMask() {
        return mask;
    }

    public void setMask(Byte mask) {
        this.mask = mask;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
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
        Ping other = (Ping) that;
        return (this.getChannelId() == null ? other.getChannelId() == null : this.getChannelId().equals(other.getChannelId()))
            && (this.getUsrId() == null ? other.getUsrId() == null : this.getUsrId().equals(other.getUsrId()))
            && (this.getDiscordId() == null ? other.getDiscordId() == null : this.getDiscordId().equals(other.getDiscordId()))
            && (this.getDiscordName() == null ? other.getDiscordName() == null : this.getDiscordName().equals(other.getDiscordName()))
            && (this.getMask() == null ? other.getMask() == null : this.getMask().equals(other.getMask()))
            && (this.getDisabled() == null ? other.getDisabled() == null : this.getDisabled().equals(other.getDisabled()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getChannelId() == null) ? 0 : getChannelId().hashCode());
        result = prime * result + ((getUsrId() == null) ? 0 : getUsrId().hashCode());
        result = prime * result + ((getDiscordId() == null) ? 0 : getDiscordId().hashCode());
        result = prime * result + ((getDiscordName() == null) ? 0 : getDiscordName().hashCode());
        result = prime * result + ((getMask() == null) ? 0 : getMask().hashCode());
        result = prime * result + ((getDisabled() == null) ? 0 : getDisabled().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", discordName=").append(discordName);
        sb.append(", mask=").append(mask);
        sb.append(", disabled=").append(disabled);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}