package cn.sola97.vrchat.controller;

import cn.sola97.vrchat.pojo.VRCEventDTO;
import cn.sola97.vrchat.pojo.impl.WsFriendContent;
import cn.sola97.vrchat.pojo.impl.WsNotificationContent;
import cn.sola97.vrchat.service.CacheService;
import cn.sola97.vrchat.service.MessageService;
import cn.sola97.vrchat.service.ScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class EventHandlerMapping {
    private static final Logger logger = LoggerFactory.getLogger(EventHandlerMapping.class);
    @Autowired
    @Lazy
    ScheduledService scheduledServiceImpl;
    @Autowired
    MessageService messageServiceImpl;
    //注入自己使AOP生效
    @Autowired
    private CacheService cacheServiceImpl;
    //注入自己使AOP生效
    @Autowired
    private EventHandlerMapping handlerMapping;

    public void handle(String type, VRCEventDTO event) {
        if ("friend-active".equals(type)) handlerMapping.friendActive(event);
        else if ("friend-location".equals(type)) handlerMapping.friendLocation(event);
        else if ("friend-offline".equals(type)) handlerMapping.friendOffline(event);
        else if ("friend-update".equals(type)) {
            scheduledServiceImpl.checkOnlineUser(((WsFriendContent) event.getContent()).getUserId());
        }
        else if ("friend-online".equals(type)) handlerMapping.friendOnline(event);
        else if ("user-active".equals(type)) handlerMapping.userActive(event);
        else if ("user-location".equals(type)) handlerMapping.userLocation(event);
        else if ("user-offline".equals(type)) handlerMapping.userOffline(event);
//        else if ("user-update".equals(type)) handlerMapping.userUpdate(event);
        else if ("user-online".equals(type)) handlerMapping.userOnline(event);
        else if ("notification".equals(type)) {
            String contentType = ((WsNotificationContent) event.getContent()).getType();
            if (contentType.equals("friendRequest")) handlerMapping.notificationFriendRequest(event);
            else if (contentType.equals("requestInvite")) handlerMapping.notificationRequestInvite(event);
            else if (contentType.equals("inviteToLocation")) handlerMapping.notificationInvite(event);
            else
                logger.warn("unsupport websocket notification type:" + contentType + "content:" + event.getContent().toString());
        } else handlerMapping.doNothing(event);
    }

    public String friendActive(VRCEventDTO<WsFriendContent> event) {
        return "active";
    }

    public String friendLocation(VRCEventDTO<WsFriendContent> event) {
        return "进入了世界";
    }

    public String friendUpdate(VRCEventDTO<WsFriendContent> event) {
        return "更新了状态";
    }

    public String friendOnline(VRCEventDTO<WsFriendContent> event) {
        return "上线了";
    }

    public String friendOffline(VRCEventDTO<WsFriendContent> event) {
        return "下线了";
    }

    public String friendAvatar(VRCEventDTO<WsFriendContent> event) {
        return "更换了角色";
    }

    public String friendDescription(VRCEventDTO<WsFriendContent> event) {
        return "修改了描述";
    }


    public String doNothing(VRCEventDTO event) {
        logger.warn("什么都不做" + event.getContent().toString());
        return "什么都不做";
    }

    public String userActive(VRCEventDTO<WsFriendContent> event) {
        return "active";
    }

    public String userLocation(VRCEventDTO<WsFriendContent> event) {
        return "进入了世界";
    }

    public String userUpdate(VRCEventDTO<WsFriendContent> event) {
        return "更新了状态";
    }

    public String userOnline(VRCEventDTO<WsFriendContent> event) {
        return "上线了";
    }

    public String userOffline(VRCEventDTO<WsFriendContent> event) {
        return "下线了";
    }

    public String notificationFriendRequest(VRCEventDTO<WsNotificationContent> event) {
        return "好友申请";
    }

    public String notificationRequestInvite(VRCEventDTO<WsNotificationContent> event) {
        return "申请Join";
    }


    public String notificationInvite(VRCEventDTO<WsNotificationContent> event) {
        return "邀请你到";
    }

    public String notificationModerated(VRCEventDTO<WsNotificationContent> event) {
        return "对你设置了 " + event.getContent().getType();
    }

}
