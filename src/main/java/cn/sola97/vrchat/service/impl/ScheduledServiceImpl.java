package cn.sola97.vrchat.service.impl;

import cn.sola97.vrchat.aop.proxy.JDAProxy;
import cn.sola97.vrchat.aop.proxy.WebSocketConnectionManagerProxy;
import cn.sola97.vrchat.controller.EventHandlerMapping;
import cn.sola97.vrchat.entity.Channel;
import cn.sola97.vrchat.entity.User;
import cn.sola97.vrchat.entity.UserOnline;
import cn.sola97.vrchat.entity.World;
import cn.sola97.vrchat.enums.ReleaseStatusEnums;
import cn.sola97.vrchat.pojo.VRCEventDTO;
import cn.sola97.vrchat.pojo.impl.WsFriendContent;
import cn.sola97.vrchat.service.*;
import cn.sola97.vrchat.utils.VRCEventDTOFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ScheduledServiceImpl implements ScheduledService {
    @Value("${bot.ownerId}")
    String ownerId;
    private final VRChatApiService vrchatApiServiceImpl;
    private final MessageService messageServiceImpl;
    private static final Logger logger = LoggerFactory.getLogger(ScheduledServiceImpl.class);
    private final ScheduledExecutorService scheduledExecutorService;
    private final WebSocketConnectionManagerProxy webSocketConnectionManager;
    private final CacheService cacheServiceImpl;
    private final Executor asyncExecutor;
    private final EventHandlerMapping eventHandlerMapping;
    private final JDAProxy jda;
    private final ChannelService channelServiceImpl;
    private Long checkOnlinePeriod;
    private Long checkChannelPeriod;

    public ScheduledServiceImpl(JDAProxy jda, ScheduledExecutorService scheduledExecutorService, WebSocketConnectionManagerProxy webSocketConnectionManagerProxy, VRChatApiService vrchatApiServiceImpl, MessageService messageServiceImpl, CacheService cacheServiceImpl, Executor asyncExecutor, EventHandlerMapping eventHandlerMapping, ChannelService channelServiceImpl,
                                @Value("${scheduled.checkOnline.period}") final Long checkOnlinePeriod,
                                @Value("${scheduled.checkChannel.period}") final Long checkChannelPeriod) {
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

        botWatchdog();
        websocketWatchdog();
        messageQueueWatchDog();
        checkOnlineUsers();
        updatePresence();
        checkChannelsTask();
        if (!scheduledExecutorService.isShutdown()) logger.info("ScheduledService running ");

    }

    private void botWatchdog() {
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                logger.debug("Discord Bot current status: " + jda.getStatus().name());
                int count = 0;
                while (this.jda.getStatus() != JDA.Status.CONNECTED && this.jda.getStatus() != JDA.Status.WAITING_TO_RECONNECT) {
                    logger.info("Discord Bot " + jda.getStatus().name() + "." + count + " seconds have passed.");
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
                    logger.info("websocketManager DISCONNECTED" + "." + count + " seconds have passed.");
                    if (count >= 10) {
                        webSocketConnectionManager.rebuild();
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

    private void messageQueueWatchDog() {
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                logger.debug("messageQueue current size:" + (messageServiceImpl.getMessageQueueSize()));
                int count = 0;
                while (messageServiceImpl.getMessageQueueSize() > 0) {
                    logger.info("messageQueue current size:" + (messageServiceImpl.getMessageQueueSize()) + "." + count + " seconds have passed.");
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

    private void checkOnlineUsers() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            logger.debug("checking online users...");
            CompletableFuture.supplyAsync(() -> vrchatApiServiceImpl.getFriends(false), asyncExecutor)
                    .thenApply(onlineFriends -> {
                        for (UserOnline onlineUser : onlineFriends) {
                            UserOnline cachedUser = cacheServiceImpl.getOnlineUser(onlineUser.getId());
                            cacheServiceImpl.setUserOnline(onlineUser);
                            String worldId = ReleaseStatusEnums.parseLocation(onlineUser.getLocation()).get("worldId");

                            if (cachedUser == null) {
                                World world = vrchatApiServiceImpl.getWorldById(worldId, true);
                                VRCEventDTO<WsFriendContent> event = VRCEventDTOFactory.createOnlineEvent((User) onlineUser, world);

                                eventHandlerMapping.friendOnline(event);

                            }
                            if (!cachedUser.getLocation().equals(cachedUser.getLocation())) {
                                World world = vrchatApiServiceImpl.getWorldById(worldId, true);
                                VRCEventDTO<WsFriendContent> event = VRCEventDTOFactory.createLocationEvent((User) onlineUser, world);

                                eventHandlerMapping.friendLocation(event);

                            }
                            if (!cachedUser.getCurrentAvatarImageUrl().equals(onlineUser.getCurrentAvatarImageUrl()) ||
                                    !cachedUser.getCurrentAvatarThumbnailImageUrl().equals(onlineUser.getCurrentAvatarThumbnailImageUrl())) {
                                World world = vrchatApiServiceImpl.getWorldById(worldId, true);
                                VRCEventDTO<WsFriendContent> event = VRCEventDTOFactory.createUpdateEvent((User) onlineUser, world);

                                eventHandlerMapping.friendAvatar(event);

                            }
                            if (!cachedUser.getStatusDescription().equals(onlineUser.getStatusDescription())) {
                                World world = vrchatApiServiceImpl.getWorldById(worldId, true);
                                VRCEventDTO<WsFriendContent> event = VRCEventDTOFactory.createUpdateEvent((User) onlineUser, world);

                                eventHandlerMapping.friendDescription(event);
                            }
                        }
                        return onlineFriends;
                    });
        }, 0, checkOnlinePeriod, TimeUnit.SECONDS);
    }

    private void checkChannelsTask() {
        scheduledExecutorService.scheduleAtFixedRate(this::checkChannelValid, 15, checkChannelPeriod, TimeUnit.SECONDS);
    }

    @Override
    public void checkChannelValid() {
        if (this.jda.getStatus() == JDA.Status.CONNECTED) {
            logger.info("正在检查Channel是否有效");
            List<Channel> channels = channelServiceImpl.selAllChannel();
            for (Channel channel : channels) {
                CompletableFuture.supplyAsync(() -> {
                    TextChannel textChannel = this.jda.getJda().getTextChannelById(channel.getChannelId());
                    //如果频道不存在，且没有被disable
                    if (textChannel == null && !channel.getDisabled()) {
                        //disable
                        channel.setDisabled(true);
                        channel.setUpdatedAt(new Date());
                        logger.info("现在无效的Channel:" + channel.getChannelName() + " id:" + channel.getChannelId());
                        //update
                        channelServiceImpl.updChannel(channel);
                    } else if (textChannel != null && channel.getDisabled()) {
                        channel.setDisabled(false);
                        channel.setUpdatedAt(new Date());
                        logger.info("现在有效的Channel: " + channel.getChannelName() + " id:" + channel.getChannelId());
                    }
                    return null;
                }, asyncExecutor);
            }
        } else {
            logger.info("检查Channel是否有效时遇到问题，Bot " + this.jda.getStatus());
        }
    }
}
