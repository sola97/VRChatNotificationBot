package cn.sola97.vrchat.service.impl;

import cn.sola97.vrchat.config.QueueConfig;
import cn.sola97.vrchat.entity.*;
import cn.sola97.vrchat.enums.ReleaseStatusEnums;
import cn.sola97.vrchat.enums.StateEnums;
import cn.sola97.vrchat.enums.StatusEnums;
import cn.sola97.vrchat.enums.TrustCorlorEnums;
import cn.sola97.vrchat.pojo.EventContent;
import cn.sola97.vrchat.pojo.MessageDTO;
import cn.sola97.vrchat.pojo.VRCEventDTO;
import cn.sola97.vrchat.pojo.impl.WsFriendContent;
import cn.sola97.vrchat.pojo.impl.WsNotificationContent;
import cn.sola97.vrchat.service.*;
import net.dv8tion.jda.api.EmbedBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {
    public static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);
    @Autowired
    SubscribeService subscribeServiceImpl;
    @Autowired
    PingService pingServiceImpl;
    @Autowired
    ChannelService channelServiceImpl;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    QueueConfig queueConfig;
    @Autowired
    CookieService cookieServiceImpl;
    @Resource
    private BlockingQueue<MessageDTO> messageBlockingQueue;
    @Autowired
    VRChatApiService vrchatApiServiceImpl;

    @Value("${bot.messageQueueKey}")
    String messageQueueKey;

    @Override
    public void setMessagesBySubscribe(VRCEventDTO event) {
        List<MessageDTO> messages = event.getMessages();
        EventContent content = (EventContent) event.getContent();
        //优先查询订阅表
        List<String> disabledChannels = channelServiceImpl.selDisabledChannel().stream().map(Channel::getChannelId).collect(Collectors.toList());
        List<Subscribe> subscribes = subscribeServiceImpl.selSubscribesByUsrIdNotInChannels(content.getUserId(), disabledChannels);
        for (Subscribe subscribe : subscribes) {
            //订阅了该用户的频道
            MessageDTO message = new MessageDTO();
            message.setType(MessageDTO.typeEnums.SEND_TO_CHANNEL);
            message.setChannelId(subscribe.getChannelId());
            //查询需要@的DiscordId
            List<Ping> pings = pingServiceImpl.selPingByChannelIdAndUserId(subscribe.getChannelId(), subscribe.getUsrId());
            for (Ping ping : pings) {
                //如果该DiscordId的用户订阅了该等级的消息，添加到提醒列表
                int mask = ping.getMask() & event.getTypeMask();
                if (mask > 0) {
                    message.addPing(ping.getDiscordId());
                }
            }
            if (!message.getPings().isEmpty()) {
                //有需要@的用户，添加消息
                messages.add(message);
            } else {
                //没有需要@的用户，按照subscribe的默认Mask来
                if ((subscribe.getMask() & event.getTypeMask()) > 0) {
                    messages.add(message);
                }
            }
            //添加消息到全部消息列表
        }
        List<String> channels = messages.stream().map(MessageDTO::getChannelId).filter(Objects::nonNull).collect(Collectors.toList());
        channels.addAll(disabledChannels);
        //如果没有查到订阅该usrId的频道，查询订阅了全部用户的频道
        List<Subscribe> subscribeAlls = subscribeServiceImpl.selSubscribesByUsrIdNotInChannels("*", channels);
        for (Subscribe subscribe : subscribeAlls) {
            MessageDTO message = new MessageDTO();
            message.setType(MessageDTO.typeEnums.SEND_TO_CHANNEL);
            message.setChannelId(subscribe.getChannelId());
            //查询需要@的DiscordId
            List<Ping> pings = pingServiceImpl.selPingByChannelIdAndUserId(subscribe.getChannelId(), "*");
            for (Ping ping : pings) {
                int mask = ping.getMask() & event.getTypeMask();
                if (mask > 0) {
                    message.addPing(ping.getDiscordId());
                }
            }
            if (!message.getPings().isEmpty()) {
                //有需要@的用户
                messages.add(message);
            } else {
                //没有需要@的用户，按照subscribe的默认Mask来
                if ((subscribe.getMask() & event.getTypeMask()) > 0) {
                    messages.add(message);
                }
            }
        }
    }

    @Override
    public void setMessageToOwner(VRCEventDTO<WsNotificationContent> event) {
        List<MessageDTO> messages = event.getMessages();
        MessageDTO message = new MessageDTO();
        String senderUserId = event.getContent().getSenderUserId();
        message.setType(MessageDTO.typeEnums.SEND_TO_OWNER);
        messages.add(message);
    }

    @Override
    public void fillFriendData(VRCEventDTO<WsFriendContent> event) {
        User user = vrchatApiServiceImpl.getUserById(event.getContent().getUserId(), false);
        event.getContent().setUser(user);
        if (user instanceof CurrentUser) {
            user.setLocation(event.getContent().getLocation());
            user.setInstanceId(event.getContent().getInstance());
            World world = vrchatApiServiceImpl.getWorldById(event.getContent().getWorld().getId(), true);
            event.getContent().setWorld(world);
        } else {
            World world = vrchatApiServiceImpl.getWorldById(user.getWorldId(), true);
            event.getContent().setWorld(world);
            event.getContent().setLocation(user.getLocation());
            event.getContent().setInstance(user.getInstanceId());
        }
    }

    @Override
    public void fillNotificationData(VRCEventDTO<WsNotificationContent> event) {
        WsNotificationContent content = event.getContent();
        String senderUserId = content.getSenderUserId();
        User sender = vrchatApiServiceImpl.getUserById(senderUserId, false);
        World world = vrchatApiServiceImpl.getWorldById(sender.getWorldId(), true);
        content.setUser(sender);
        content.setWorld(world);
    }

    @Override
    public void
    setEmbed(User user, @Nullable World world, MessageDTO message) {
        Map<String, String> locationMap = ReleaseStatusEnums.parseLocation(user.getLocation());
        if (locationMap.get("usrId") != null)
            locationMap.put("username", vrchatApiServiceImpl.getUserById(locationMap.get("usrId"), true).getDisplayName());
        else
            locationMap.put("username", "");
        Instant now = Instant.now();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(TrustCorlorEnums.getByTags(user.getTags()));
        String userUrl = user.getUsername().startsWith("steam_") ?
                "https://steamcommunity.com/profiles/" + user.getUsername().replaceAll("steam_", "") : null;
        Integer friendIndex = user.getFriendIndex();
        String indexString = "";
        if (!user.getFriend()) {
            indexString = "  [非好友]";
        } else if (friendIndex == null) {
            friendIndex = cookieServiceImpl.getCurrentUserFriendIndex(user.getId());
        }
        if (friendIndex != null) {
            indexString = "  [" + friendIndex + "]";
        }
        embedBuilder.setAuthor(user.getDisplayName() + indexString, userUrl, user.getCurrentAvatarThumbnailImageUrl());
        embedBuilder.setTimestamp(now);
        if (!user.getFriend()) {
            //不是好友,看不到
            embedBuilder.setFooter(" - ", StateEnums.getUrlFromString("offline"));
        } else if ("online".equals(user.getState())) {
            //用户正在游戏中status  joinme/active/busy
            embedBuilder.setFooter(String.join(" ", user.getStatus(), user.getStatusDescription()), StatusEnums.getUrlFromString(user.getStatus()));
        } else {
            //用户不在游戏 state online/offline/active
            embedBuilder.setFooter(String.join(" ", user.getState(), user.getStatusDescription()), StateEnums.getUrlFromString(user.getState()));
        }
        Optional.ofNullable(world).map(World::getThumbnailImageUrl).map(embedBuilder::setThumbnail);
        message.setLocationMap(locationMap);
        message.setEmbedBuilder(embedBuilder);
    }

    @Override
    public Long enqueueMessages(List<MessageDTO> messages) {
        Long index = (long) 0;
        for (MessageDTO message : messages) {
            try {
                messageBlockingQueue.put(message);
                index++;
            } catch (Exception e) {
                logger.error("入队出错 message.toString():" + message.toString(), e);
            }
        }
        logger.debug("入队：" + index + " Queue size：" + messageBlockingQueue.size());
        return index;
    }

    @Override
    public int getMessageQueueSize() {
        return messageBlockingQueue.size();
    }
}
