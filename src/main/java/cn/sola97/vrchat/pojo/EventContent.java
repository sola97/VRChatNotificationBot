package cn.sola97.vrchat.pojo;

import cn.sola97.vrchat.entity.User;
import cn.sola97.vrchat.entity.World;

public abstract class EventContent {
    protected User user;
    protected World world;

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

    public abstract String getUserId();
}
