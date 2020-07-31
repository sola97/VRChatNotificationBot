package cn.sola97.vrchat.aop.aspect;

import cn.sola97.vrchat.entity.World;
import cn.sola97.vrchat.pojo.MessageDTO;
import cn.sola97.vrchat.pojo.VRCEventDTO;
import cn.sola97.vrchat.pojo.impl.WsFriendContent;
import cn.sola97.vrchat.pojo.impl.WsNotificationContent;
import cn.sola97.vrchat.service.CacheService;
import cn.sola97.vrchat.utils.TimeUtil;
import cn.sola97.vrchat.utils.WorldUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Component
@Aspect
@Order(2)
public class FriendActionAspect {
    private Logger logger = LoggerFactory.getLogger(FriendActionAspect.class);
    @Autowired
    CacheService cacheServiceImpl;
    @Autowired
    TimeUtil timeUtil;
    @Value("${cache.online.expire}")
    long expirePeriod;

    @Pointcut("execution(* cn.sola97.vrchat.controller.EventHandlerMapping.*Update(..))")
    public void friendUpdate() {
    }

    @Pointcut("execution(* cn.sola97.vrchat.controller.EventHandlerMapping.friendAvatar(..))")
    public void friendAvatar() {
    }

    @Pointcut("execution(* cn.sola97.vrchat.controller.EventHandlerMapping.friendDescription(..))")
    public void friendDescription() {
    }

    @Pointcut("execution(* cn.sola97.vrchat.controller.EventHandlerMapping.*Online(..))")
    public void friendOnline() {
    }

    @Pointcut("execution(* cn.sola97.vrchat.controller.EventHandlerMapping.*Location(..))")
    public void friendLocation() {
    }

    @Pointcut("execution(* cn.sola97.vrchat.controller.EventHandlerMapping.friendOffline(..))")
    public void friendOffline() {
    }

    @Pointcut("execution(* cn.sola97.vrchat.controller.EventHandlerMapping.friendActive(..))")
    public void friendActive() {
    }

    @Pointcut("execution(* cn.sola97.vrchat.controller.EventHandlerMapping.notificationFriendRequest(..))")
    public void friendRequest() {
    }

    @Pointcut("execution(* cn.sola97.vrchat.controller.EventHandlerMapping.notificationInvite(..))")
    public void friendInvite() {
    }

    @Pointcut("execution(* cn.sola97.vrchat.controller.EventHandlerMapping.notificationRequestInvite(..))")
    public void friendRequestInvite() {
    }

    @Pointcut("execution(* cn.sola97.vrchat.controller.EventHandlerMapping.notificationModerated(..))")
    public void playerModerated() {
    }

    @Before("(friendOnline() || friendLocation() || friendUpdate() || friendAvatar() || friendDescription()) && args(event)")
    public void onlineLog(VRCEventDTO<WsFriendContent> event) {
        logger.debug("onlineLog advise before " + event.getContent().getUserId());
    }

    @Around("(friendOnline() || friendLocation() || friendUpdate() || friendAvatar() || friendDescription()) && args(event)")
    public Object setLocation(ProceedingJoinPoint point, VRCEventDTO<WsFriendContent> event) throws Throwable {
        logger.debug("setLocation for " + event.toString());
        Object proceed = point.proceed();
        logger.debug("proceed " + point.getClass());
        String description = proceed.toString();
        List<MessageDTO> messages = event.getMessages();
        World world = event.getContent().getWorld();
        for (MessageDTO message : messages) {
            EmbedBuilder embedBuilder = message.getEmbedBuilder();
            Map<String, String> locationMap = message.getLocationMap();
            String instance = event.getContent().getInstance();
            String value = WorldUtil.convertToString(world, locationMap, instance);
            embedBuilder.addField(description, value, true);
        }
        return proceed;
    }

    @After("friendAvatar() && args(event)")
    public void setAvatar(VRCEventDTO<WsFriendContent> event) {
        for (MessageDTO message : event.getMessages()) {
            message.getEmbedBuilder().setImage(event.getContent().getUser().getCurrentAvatarThumbnailImageUrl());
        }
    }

    @After("friendDescription() && args(event)")
    public void setDescription(VRCEventDTO<WsFriendContent> event) {
        for (MessageDTO message : event.getMessages()) {
            EmbedBuilder embedBuilder = message.getEmbedBuilder();
            MessageEmbed.Field field = embedBuilder.getFields().get(0);
            String name = field.getName() + "：" + event.getContent().getUser().getStatusDescription();
            embedBuilder.clearFields();
            embedBuilder.addField(name, field.getValue(), true);
        }
    }

    @Around(value = "friendOffline() && args(event)")
    public Object setOffline(ProceedingJoinPoint point, VRCEventDTO<WsFriendContent> event) throws Throwable {
        Object proceed = point.proceed();
        String description = proceed.toString();
        logger.debug("setOffline：" + description);
        List<MessageDTO> messages = event.getMessages();
        for (MessageDTO message : messages) {
            EmbedBuilder embedBuilder = message.getEmbedBuilder();
            embedBuilder.addField(description, event.getContent().getWorld().getName(), true);
            ZonedDateTime time = cacheServiceImpl.getUserOfflineTime(event.getContent().getUserId());
            embedBuilder.addField("时间", timeUtil.formatTime(time), true);
            embedBuilder.setTimestamp(time);
        }

        return proceed;
    }

    @Around(value = "friendActive() && args(event)")
    public Object setOnline(ProceedingJoinPoint point, VRCEventDTO<WsFriendContent> event) throws Throwable {
        Object proceed = point.proceed();
        String description = proceed.toString();
        logger.debug("setActive：" + description);
        List<MessageDTO> messages = event.getMessages();
        for (MessageDTO message : messages) {
            EmbedBuilder embedBuilder = message.getEmbedBuilder();
            embedBuilder.setDescription(description);
            embedBuilder.addField("上次登录", timeUtil.formatTime(event.getContent().getUser().getLast_login()), true);
        }
        return proceed;
    }

    @Around(value = "friendRequest() && args(event)")
    public Object setFriendRequest(ProceedingJoinPoint point, VRCEventDTO<WsNotificationContent> event) throws Throwable {
        Object proceed = point.proceed();
        String description = proceed.toString();
        logger.debug("setFriendRequest：" + event.getContent().getSenderUsername() + description);
        List<MessageDTO> messages = event.getMessages();
        for (MessageDTO message : messages) {
            EmbedBuilder embedBuilder = message.getEmbedBuilder();
            embedBuilder.setDescription(description);
            embedBuilder.addField("时间", timeUtil.formatTime(event.getContent().getCreated_at()), true);
        }
        return proceed;
    }

    @Around(value = "friendInvite() && args(event)")
    public Object setFriendInvite(ProceedingJoinPoint point, VRCEventDTO<WsNotificationContent> event) throws Throwable {
        Object proceed = point.proceed();
        String description = proceed.toString() + " " + event.getContent().getDetails().getWorldName();
        logger.debug("setFriendInvite：" + event.getContent().getSenderUsername() + description);
        List<MessageDTO> messages = event.getMessages();
        for (MessageDTO message : messages) {
            EmbedBuilder embedBuilder = message.getEmbedBuilder();
            embedBuilder.setDescription(description);
            embedBuilder.addField("时间", timeUtil.formatTime(event.getContent().getCreated_at()), true);
        }
        return proceed;
    }


    @Around(value = "friendRequestInvite() && args(event)")
    public Object setFriendRequestInvite(ProceedingJoinPoint point, VRCEventDTO<WsNotificationContent> event) throws Throwable {
        Object proceed = point.proceed();
        String description = proceed.toString();
        logger.debug("setFriendRequestInvite：" + event.getContent().getSenderUsername() + description);
        List<MessageDTO> messages = event.getMessages();
        for (MessageDTO message : messages) {
            EmbedBuilder embedBuilder = message.getEmbedBuilder();
            embedBuilder.setDescription(description);
            embedBuilder.addField("时间", timeUtil.formatTime(event.getContent().getCreated_at()), true);
        }
        return proceed;
    }

    @Around(value = "playerModerated() && args(event)")
    public Object setPlayerModerated(ProceedingJoinPoint point, VRCEventDTO<WsNotificationContent> event) throws Throwable {
        Object proceed = point.proceed();
        String description = proceed.toString();
        logger.debug("setPlayerModeratedIds：{} {}", event.getContent().getSenderUsername(), description);
        List<MessageDTO> messages = event.getMessages();
        for (MessageDTO message : messages) {
            EmbedBuilder embedBuilder = message.getEmbedBuilder();
            embedBuilder.setDescription(description);
            embedBuilder.addField("时间", timeUtil.formatTime(event.getContent().getCreated_at()), true);
        }
        return proceed;
    }

}
