package cn.sola97.vrchat.utils;

import cn.sola97.vrchat.entity.Moderation;
import cn.sola97.vrchat.entity.User;
import cn.sola97.vrchat.entity.World;
import cn.sola97.vrchat.enums.EventTypeEnums;
import cn.sola97.vrchat.pojo.VRCEventDTO;
import cn.sola97.vrchat.pojo.impl.WsFriendContent;
import cn.sola97.vrchat.pojo.impl.WsNotificationContent;

public class VRCEventDTOFactory {
    public static VRCEventDTO<WsFriendContent> createOfflineEvent(String usrId, User user) {
        VRCEventDTO<WsFriendContent> event = new VRCEventDTO<>(true);
        event.setType(EventTypeEnums.OFFLINE);
        WsFriendContent content = new WsFriendContent();
        content.setUserId(usrId);
        content.setUser(user);
        event.setContent(content);
        return event;
    }

    public static VRCEventDTO<WsFriendContent> createOnlineEvent(User user, World world) {
        VRCEventDTO<WsFriendContent> event = new VRCEventDTO<>(true);
        event.setType(EventTypeEnums.ONLINE);
        event.setContent(createContent(user, world));
        return event;
    }

    public static VRCEventDTO<WsFriendContent> createLocationEvent(User user, World world) {
        VRCEventDTO<WsFriendContent> event = new VRCEventDTO<>(true);
        event.setType(EventTypeEnums.LOCATION);
        event.setContent(createContent(user, world));
        return event;
    }

    public static VRCEventDTO<WsFriendContent> createUpdateEvent(User user, World world) {
        VRCEventDTO<WsFriendContent> event = new VRCEventDTO<>(true);
        event.setType(EventTypeEnums.UPDATE);
        event.setContent(createContent(user, world));
        return event;
    }

    public static VRCEventDTO<WsNotificationContent> createNotificationEvent(Moderation moderation) {
        VRCEventDTO<WsNotificationContent> event = new VRCEventDTO<>(true);
        event.setType(EventTypeEnums.NOTIFICATION);
        event.setContent(createContent(moderation));
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

    private static WsNotificationContent createContent(Moderation moderation) {
        WsNotificationContent content = new WsNotificationContent();
        content.setCreated_at(moderation.getCreated());
        content.setReceiverUserId(moderation.getTargetUserId());
        content.setSenderUserId(moderation.getSourceUserId());
        content.setSenderUsername(moderation.getSourceDisplayName());
        content.setType(moderation.getType());
        return content;
    }
}
