package cn.sola97.vrchat.pojo;

import cn.sola97.vrchat.entity.User;
import cn.sola97.vrchat.entity.World;

import java.time.ZonedDateTime;

public abstract class EventContent {
    protected User user;
    protected World world;
    protected ZonedDateTime createdAt;

    public EventContent() {
    }

    public abstract String getUserId();

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "EventContent{" +
                "user=" + user +
                ", world=" + world +
                ", createdAt=" + createdAt +
                '}';
    }

}
