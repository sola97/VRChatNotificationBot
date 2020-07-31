package cn.sola97.vrchat.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Moderation {

    private String id;
    private String sourceDisplayName;
    private String sourceUserId;
    private String targetDisplayName;
    private String targetUserId;
    private String type;
    private Date created;

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceDisplayName() {
        return sourceDisplayName;
    }

    public void setSourceDisplayName(String sourceDisplayName) {
        this.sourceDisplayName = sourceDisplayName;
    }

    public String getSourceUserId() {
        return sourceUserId;
    }

    public void setSourceUserId(String sourceUserId) {
        this.sourceUserId = sourceUserId;
    }

    public String getTargetDisplayName() {
        return targetDisplayName;
    }

    public void setTargetDisplayName(String targetDisplayName) {
        this.targetDisplayName = targetDisplayName;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Moderation{" +
                "created=" + created +
                ", id='" + id + '\'' +
                ", sourceDisplayName='" + sourceDisplayName + '\'' +
                ", sourceUserId='" + sourceUserId + '\'' +
                ", targetDisplayName='" + targetDisplayName + '\'' +
                ", targetUserId='" + targetUserId + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Moderation that = (Moderation) o;
        return id.equals(that.id) &&
                Objects.equals(sourceDisplayName, that.sourceDisplayName) &&
                Objects.equals(sourceUserId, that.sourceUserId) &&
                Objects.equals(targetDisplayName, that.targetDisplayName) &&
                Objects.equals(targetUserId, that.targetUserId) &&
                Objects.equals(type, that.type) &&
                Objects.equals(created, that.created);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sourceDisplayName, sourceUserId, targetDisplayName, targetUserId, type, created);
    }
}
