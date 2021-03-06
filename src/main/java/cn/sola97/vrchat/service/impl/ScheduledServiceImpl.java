package cn.sola97.vrchat.service.impl;

import cn.sola97.vrchat.aop.proxy.JDAProxy;
import cn.sola97.vrchat.aop.proxy.WebSocketConnectionManagerProxy;
import cn.sola97.vrchat.controller.EventHandlerMapping;
import cn.sola97.vrchat.entity.*;
import cn.sola97.vrchat.enums.EventTypeEnums;
import cn.sola97.vrchat.enums.WorldInstanceEnums;
import cn.sola97.vrchat.pojo.VRCEventDTO;
import cn.sola97.vrchat.pojo.impl.WsFriendContent;
import cn.sola97.vrchat.pojo.impl.WsNotificationContent;
import cn.sola97.vrchat.service.*;
import cn.sola97.vrchat.utils.VRCEventDTOFactory;
import cn.sola97.vrchat.utils.WorldUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ScheduledServiceImpl implements ScheduledService {
    @Value("${bot.ownerId}")
    String ownerId;
    private final VRChatApiService vrchatApiServiceImpl;
    private final MessageService messageServiceImpl;
    private final PingServiceImpl pingServiceImpl;
    private static final Logger logger = LoggerFactory.getLogger(ScheduledServiceImpl.class);
    private final ScheduledExecutorService scheduledExecutorService;
    private final WebSocketConnectionManagerProxy webSocketConnectionManager;
    private final CacheService cacheServiceImpl;
    private final Executor asyncExecutor;
    private final EventHandlerMapping eventHandlerMapping;
    private final JDAProxy jda;
    private final ChannelService channelServiceImpl;
    private final SubscribeService subscribeServiceImpl;
    private final CookieService cookieServiceImpl;
    private Long checkOnlinePeriod;
    private Long checkChannelPeriod;
    private Long checkNonFriendPeriod;
    private Long checkModeratedPeriod;

    public ScheduledServiceImpl(JDAProxy jda, ScheduledExecutorService scheduledExecutorService,
                                WebSocketConnectionManagerProxy webSocketConnectionManagerProxy,
                                VRChatApiService vrchatApiServiceImpl, MessageService messageServiceImpl,
                                PingServiceImpl pingServiceImpl, CacheService cacheServiceImpl,
                                Executor asyncExecutor, EventHandlerMapping eventHandlerMapping,
                                ChannelService channelServiceImpl,
                                SubscribeService subscribeServiceImpl,
                                CookieService cookieServiceImpl,
                                @Value("${scheduled.checkOnline.period}") final Long checkOnlinePeriod,
                                @Value("${scheduled.checkChannel.period}") final Long checkChannelPeriod,
                                @Value("${scheduled.checkNonfriend.period}") final Long checkNonFriendPeriod,
                                @Value("${vrchat.currentUser.moderated.period}") final Long checkModeratedPeriod) {
        this.pingServiceImpl = pingServiceImpl;
        this.subscribeServiceImpl = subscribeServiceImpl;
        this.cookieServiceImpl = cookieServiceImpl;
        this.checkOnlinePeriod = checkOnlinePeriod;
        this.checkChannelPeriod = checkChannelPeriod;
        this.jda = jda;
        this.scheduledExecutorService = scheduledExecutorService;
        this.webSocketConnectionManager = webSocketConnectionManagerProxy;
        this.vrchatApiServiceImpl = vrchatApiServiceImpl;
        this.messageServiceImpl = messageServiceImpl;
        this.cacheServiceImpl = cacheServiceImpl;
        this.asyncExecutor = asyncExecutor;
        this.eventHandlerMapping = eventHandlerMapping;
        this.channelServiceImpl = channelServiceImpl;
        this.checkNonFriendPeriod = checkNonFriendPeriod;
        this.checkModeratedPeriod = checkModeratedPeriod;

        botWatchdog();
        websocketWatchdog();
        messageQueueWatchDog();
        checkOnlineUsers();
        updatePresence();
        checkChannelsTask();
        checkNonFriendAvatar();
//        playerModeratedCheck();
        if (!scheduledExecutorService.isShutdown()) logger.info("ScheduledService running ");

    }

    private void botWatchdog() {
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                logger.debug("Discord Bot current status: " + jda.getStatus().name());
                int count = 0;
                while (this.jda.getStatus() != JDA.Status.CONNECTED && this.jda.getStatus() != JDA.Status.WAITING_TO_RECONNECT) {
                    logger.warn("Discord Bot " + jda.getStatus().name() + "." + count + " seconds have passed.");
                    if (count >= 30) {
                        jda.rebuild();
                        break;
                    }
                    Thread.sleep(1000);
                    count++;
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }, 10, 5, TimeUnit.SECONDS);
    }

    private void websocketWatchdog() {
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                logger.debug("websocketManager current status:" + (webSocketConnectionManager.isConnected() ? "CONNECTED" : "DISCONNECTED"));
                int count = 0;
                while (!this.webSocketConnectionManager.isConnected()) {
                    logger.warn("websocketManager DISCONNECTED" + "." + count + " seconds have passed.");
                    if (count >= 10) {
                        webSocketConnectionManager.rebuild();
                        break;
                    }
                    Thread.sleep(1000);
                    count++;
                }
            } catch (Exception e) {
                logger.error("websocketWatchdog error", e);
            }
        }, 10, 5, TimeUnit.SECONDS);
    }

    private void messageQueueWatchDog() {
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                logger.debug("messageQueue current size:{}", messageServiceImpl.getMessageQueueSize());
                int count = 0;
                while (messageServiceImpl.getMessageQueueSize() > 0) {
                    logger.warn("messageQueue current size:{}. {} seconds have passed.", messageServiceImpl.getMessageQueueSize(), count);
                    if (count >= 30) {
                        jda.rebuild();
                        break;
                    }
                    Thread.sleep(1000);
                    count++;
                }
            } catch (Exception e) {
                logger.error("消息队列出错：", e);
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    private void updatePresence() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            if (webSocketConnectionManager.isConnected()) {
                jda.getJda().getPresence().setActivity(Activity.playing("VRChat Connected"));
            } else {
                jda.getJda().getPresence().setActivity(Activity.playing("VRChat Disconnected"));
            }
        }, 10, 5, TimeUnit.SECONDS);
    }

    public Integer checkOnlineUser(String usrId) {
        User user = vrchatApiServiceImpl.getUserById(usrId, false);
        return checkOnlineUser(user);
    }

    private Integer checkOnlineUser(UserOnline onlineUser) {
        int count = 0;
        UserOnline cachedUser = cacheServiceImpl.getOnlineUser(onlineUser.getId());
        String worldId = WorldInstanceEnums.parseLocation(onlineUser.getLocation()).get("worldId");
        if (cachedUser == null) {
            World world = vrchatApiServiceImpl.getWorldById(worldId, true);
            VRCEventDTO<WsFriendContent> event = VRCEventDTOFactory.createOnlineEvent((User) onlineUser, world);
            logger.info("轮询-好友：没有获取到好友{} 的缓存数据，发送上线通知", onlineUser.getDisplayName());
            eventHandlerMapping.friendOnline(event);
            count++;
            return count;
        }
        cacheServiceImpl.setUserOnline(onlineUser);
        if (cachedUser.getLocation() != null && !cachedUser.getLocation().equals(onlineUser.getLocation())) {
            Map<String, String> newLocationMap = WorldInstanceEnums.parseLocation(onlineUser.getLocation());
            Map<String, String> oldLocationMap = WorldInstanceEnums.parseLocation(cachedUser.getLocation());
            World newWorld = vrchatApiServiceImpl.getWorldById(newLocationMap.get("worldId"), true);
            World oldWorld = vrchatApiServiceImpl.getWorldById(oldLocationMap.get("worldId"), true);
            if (oldLocationMap.get("usrId") != null)
                oldLocationMap.put("username", vrchatApiServiceImpl.getUserById(oldLocationMap.get("usrId"), true).getDisplayName());
            if (newLocationMap.get("usrId") != null)
                newLocationMap.put("username", vrchatApiServiceImpl.getUserById(newLocationMap.get("usrId"), true).getDisplayName());
            VRCEventDTO<WsFriendContent> event = VRCEventDTOFactory.createLocationEvent((User) onlineUser, newWorld);
            logger.info("轮询-好友：{}更换了地图 {} -> {}", cachedUser.getDisplayName(),
                    WorldUtil.convertToStringOneLine(oldWorld, oldLocationMap),
                    WorldUtil.convertToStringOneLine(newWorld, newLocationMap));
            eventHandlerMapping.friendLocation(event);
            count++;
        }
        if (cachedUser.getCurrentAvatarThumbnailImageUrl() != null &&
                cachedUser.getCurrentAvatarThumbnailImageUrl() != null &&
                (!cachedUser.getCurrentAvatarImageUrl().equals(onlineUser.getCurrentAvatarImageUrl()) ||
                        !cachedUser.getCurrentAvatarThumbnailImageUrl().equals(onlineUser.getCurrentAvatarThumbnailImageUrl()))) {
            World world = vrchatApiServiceImpl.getWorldById(worldId, true);
            VRCEventDTO<WsFriendContent> event = VRCEventDTOFactory.createUpdateEvent((User) onlineUser, world);
            logger.info("轮询-好友：{}更换了角色 {} -> {}", cachedUser.getDisplayName(), cachedUser.getCurrentAvatarImageUrl(), onlineUser.getCurrentAvatarImageUrl());
            eventHandlerMapping.friendAvatar(event);
            count++;

        }
        if (cachedUser.getStatusDescription() != null && !cachedUser.getStatusDescription().equals(onlineUser.getStatusDescription())) {
            World world = vrchatApiServiceImpl.getWorldById(worldId, true);
            VRCEventDTO<WsFriendContent> event = VRCEventDTOFactory.createUpdateEvent((User) onlineUser, world);
            logger.info("轮询-好友：{}更换了描述 {} -> {}", cachedUser.getDisplayName(), cachedUser.getStatusDescription(), onlineUser.getStatusDescription());
            eventHandlerMapping.friendDescription(event);
            count++;
        }
        return count;
    }

    private void checkOnlineUsers() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            logger.info("开始在线好友状态检查...");
            List<UserOnline> friends = vrchatApiServiceImpl.getFriends(false);
            AtomicInteger count = new AtomicInteger(0);
            friends.forEach(onlineUser -> {
                CompletableFuture.supplyAsync(() -> checkOnlineUser(onlineUser), asyncExecutor)
                        .thenAccept(count::addAndGet)
                        .exceptionally(e -> {
                            logger.error("checkOnlineUsers出错:{}", Optional.ofNullable(onlineUser).map(UserOnline::getDisplayName).get(), e);
                            return null;
                        });
            });
            logger.info("结束在线好友状态检查 总事件数：{}", count.get());
        }, 0, checkOnlinePeriod, TimeUnit.SECONDS);
    }

    private void checkChannelsTask() {
        scheduledExecutorService.scheduleAtFixedRate(this::checkChannelValid, 15, checkChannelPeriod, TimeUnit.SECONDS);
    }

    @Override
    public void checkChannelValid() {
        if (this.jda.getStatus() == JDA.Status.CONNECTED) {
            logger.info("正在检查Channel是否有效...");
            List<Channel> channels = channelServiceImpl.selAllChannel();
            Integer success = 0;
            Integer error = 0;
            for (Channel channel : channels) {
                try {
                    TextChannel textChannel = this.jda.getJda().getTextChannelById(channel.getChannelId());
                    //如果频道不存在，且没有被disable
                    if (textChannel == null && !channel.getDisabled()) {
                        //disable
                        channel.setDisabled(true);
                        channel.setUpdatedAt(new Date());

                        logger.info("现在无效的Channel:" + channel.getChannelName() + " id:" + channel.getChannelId());
                        channelServiceImpl.updChannelByPrimaryKey(channel);
                    } else if (textChannel != null && channel.getDisabled()) {
                        channel.setDisabled(false);
                        channel.setUpdatedAt(new Date());

                        logger.info("现在有效的Channel: " + channel.getChannelName() + " id:" + channel.getChannelId());
                        channelServiceImpl.updChannelByPrimaryKey(channel);
                    }
                    success++;
                } catch (Exception e) {
                    logger.error("检查Channel：" + channel.getChannelName() + "  ----  " + channel.getChannelId() + "出错 \n", e.getMessage());
                    error++;
                }
            }
            logger.info("Channel状态检查结束 success：{} error:{}", channels.size(), error);
        } else {
            logger.warn("检查Channel是否有效时遇到问题，Bot stats:{} 本轮结束", this.jda.getStatus());
        }
    }

    private void checkNonFriendAvatar() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            logger.info("checking non-friend avatar...");
            List<String> friendUserIds = cookieServiceImpl.getCurrentUser().getFriends();
            friendUserIds.add("*");
            logger.info("获取到好友总数：{}", friendUserIds.size());
            List<Subscribe> subscribes = subscribeServiceImpl.selAllSubscribesNotInUsrIdList(friendUserIds);
            List<Ping> pings = pingServiceImpl.selAllPingNotInUsrIdList(friendUserIds);
            logger.debug("获取到非好友subscribes记录：{}", subscribes.size());
            logger.debug("获取到非好友pings记录：{}", pings.size());
            Map<String, String> usrIds = new HashMap<>();
            subscribes.stream()
                    .filter(subscribe -> (subscribe.getMask() & EventTypeEnums.UPDATE.getMask()) > 0)
                    .forEach(subscribe -> usrIds.put(subscribe.getUsrId(), subscribe.getDisplayName()));
            pings.stream()
                    .filter(ping -> (ping.getMask() & EventTypeEnums.UPDATE.getMask()) > 0)
                    .forEach(ping -> {
                        if (!usrIds.containsKey(ping.getUsrId())) {
                            usrIds.put(ping.getUsrId(), "");
                        }
                    });
            logger.info("获取到非好友订阅总数：{}", usrIds.size());
            usrIds.forEach((usrId, displayName) -> {
                CompletableFuture.runAsync(() -> {
                    logger.info("正在检查用户{}  ---  ID:{}", displayName, usrId);
                    User nonFriendUser = vrchatApiServiceImpl.getUserById(usrId, false);
                    User cachedUser = cacheServiceImpl.getNonFriendUser(usrId);
                    cacheServiceImpl.setNonFriendUser(nonFriendUser);
                    if (cachedUser == null) {
                        logger.info("用户当前没有缓存 {}  ---  ID:{}", displayName, usrId);
                        return;
                    }
                    if (cachedUser.getId() == null) {
                        logger.info("用户数据不存在 {}  ---  ID:{}", displayName, usrId);
                        return;
                    }
                    if (!cachedUser.getCurrentAvatarImageUrl().equals(nonFriendUser.getCurrentAvatarImageUrl()) ||
                            !cachedUser.getCurrentAvatarThumbnailImageUrl().equals(nonFriendUser.getCurrentAvatarThumbnailImageUrl())) {
                        VRCEventDTO<WsFriendContent> event = VRCEventDTOFactory.createUpdateEvent(nonFriendUser, new World());
                        logger.info("非好友：{}更换了角色", displayName);
                        eventHandlerMapping.friendAvatar(event);
                    }
                    if (nonFriendUser.getBio() != null && !nonFriendUser.getBio().equals(cachedUser.getBio())) {
                        VRCEventDTO<WsFriendContent> event = VRCEventDTOFactory.createUpdateEvent(nonFriendUser, new World());
                        logger.info("非好友：{}更换了描述", displayName);
                        eventHandlerMapping.friendDescription(event);
                    }
                }, asyncExecutor).exceptionally(e -> {
                    logger.error("检查用户：" + displayName + "出错 \n", e);
                    return null;
                });
            });

        }, 10, checkNonFriendPeriod, TimeUnit.SECONDS);
    }

    private void playerModeratedCheck() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                logger.info("开始检查PlayerModerated...");
                List<Moderation> newModerations = vrchatApiServiceImpl.getPlayerModerated();
                List<Moderation> diff = cacheServiceImpl.diffPlayerModerated(newModerations);
                if (!diff.isEmpty()) {
                    for (Moderation moderation : diff) {
                        VRCEventDTO<WsNotificationContent> notificationEvent = VRCEventDTOFactory.createNotificationEvent(moderation);
                        logger.info("轮询-Moderated：{} {} {}了 {}",
                                moderation.getId(), moderation.getSourceDisplayName(), moderation.getType(), moderation.getTargetDisplayName());
                        eventHandlerMapping.notificationModerated(notificationEvent);
                    }
                }
                logger.info("检查PlayerModerated结束，事件数:{}", diff.size());
            } catch (Exception e) {
                cacheServiceImpl.deletePlayerModeratedIds();
                logger.error("检查PlayerModerated出错", e);
            }
        }, 15, checkModeratedPeriod, TimeUnit.SECONDS);
    }
}
