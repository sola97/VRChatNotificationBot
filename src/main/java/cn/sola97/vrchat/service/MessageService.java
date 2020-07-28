package cn.sola97.vrchat.service;

import cn.sola97.vrchat.entity.User;
import cn.sola97.vrchat.entity.World;
import cn.sola97.vrchat.pojo.MessageDTO;
import cn.sola97.vrchat.pojo.VRCEventDTO;
import cn.sola97.vrchat.pojo.impl.WsFriendContent;
import cn.sola97.vrchat.pojo.impl.WsNotificationContent;

import javax.annotation.Nullable;
import java.util.List;

public interface MessageService {
    void setMessagesBySubscribe(VRCEventDTO event);

    void setMessageToOwner(VRCEventDTO<WsNotificationContent> event);

    void fillFriendData(VRCEventDTO<WsFriendContent> event);

    void fillNotificationData(VRCEventDTO<WsNotificationContent> event);

    void setEmbed(User user, @Nullable World world, MessageDTO message);

    Long enqueueMessages(List<MessageDTO> queue);

    Long getMessageQueueSize();
}
