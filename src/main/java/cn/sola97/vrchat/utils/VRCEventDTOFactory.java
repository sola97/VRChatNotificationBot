package cn.sola97.vrchat.utils;

import cn.sola97.vrchat.entity.User;
import cn.sola97.vrchat.entity.World;
import cn.sola97.vrchat.enums.EventTypeEnums;
import cn.sola97.vrchat.pojo.VRCEventDTO;
import cn.sola97.vrchat.pojo.impl.WsFriendContent;

public class VRCEventDTOFactory {
    public static VRCEventDTO<WsFriendContent> createOfflineEvent(String usrId) {
        VRCEventDTO<WsFriendContent> event = new VRCEventDTO<>();
        event.setType(EventTypeEnums.OFFLINE);
        WsFriendContent content = new WsFriendContent();
        content.setUserId(usrId);
        event.setContent(content);
        return event;
    }

    public static VRCEventDTO<WsFriendContent> createOnlineEvent(User user, World world) {
        VRCEventDTO<WsFriendContent> event = new VRCEventDTO<>();
        event.setType(EventTypeEnums.ONLINE);
        event.setContent(createContent(user, world));
        return event;
    }

    public static VRCEventDTO<WsFriendContent> createLocationEvent(User user, World world) {
        VRCEventDTO<WsFriendContent> event = new VRCEventDTO<>();
        event.setType(EventTypeEnums.LOCATION);
        event.setContent(createContent(user, world));
        return event;
    }

    public static VRCEventDTO<WsFriendContent> createUpdateEvent(User user, World world) {
        VRCEventDTO<WsFriendContent> event = new VRCEventDTO<>();
        event.setType(EventTypeEnums.UPDATE);
        event.setContent(createContent(user, world));
        return event;
    }

    private static WsFriendContent createContent(User user, World world) {
        WsFriendContent content = new WsFriendContent();
        content.setUserId(user.getId());
        content.setUser(user);
        content.setInstance(user.getLocation());
        content.setLocation(user.getLocation());
        content.setWorld(world);
        return content;
    }
}
