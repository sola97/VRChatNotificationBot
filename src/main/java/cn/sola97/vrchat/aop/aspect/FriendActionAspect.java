package cn.sola97.vrchat.aop.aspect;

import cn.sola97.vrchat.entity.World;
import cn.sola97.vrchat.pojo.EventContent;
import cn.sola97.vrchat.pojo.MessageDTO;
import cn.sola97.vrchat.pojo.VRCEventDTO;
import cn.sola97.vrchat.pojo.impl.WsFriendContent;
import cn.sola97.vrchat.pojo.impl.WsNotificationContent;
import cn.sola97.vrchat.service.CacheService;
import cn.sola97.vrchat.utils.TimeUtil;
import cn.sola97.vrchat.utils.WorldUtil;
import com.mysql.cj.util.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
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

    @Pointcut("execution(* cn.sola97.vrchat.controller.EventHandlerMapping.friendAvatar(..))")
    public void friendAvatar() {
    }

    @Pointcut("execution(* cn.sola97.vrchat.controller.EventHandlerMapping.friendDescription(..))")
    public void friendDescription() {
    }

    @Pointcut("execution(* cn.sola97.vrchat.controller.EventHandlerMapping.*Online(..))")
    public void allOnline() {
    }

    @Pointcut("execution(* cn.sola97.vrchat.controller.EventHandlerMapping.*Location(..))")
    public void allLocation() {
    }

    @Pointcut("execution(* cn.sola97.vrchat.controller.EventHandlerMapping.*Update(..))")
    public void allUpdate() {
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

//    @Before("(allOnline() || allLocation() || allUpdate() || friendAvatar() || friendDescription()) && args(event)")
//    public void onlineLog(VRCEventDTO<WsFriendContent> event) {
//        logger.debug("onlineLog advise before " + event.getContent().getUserId());
//    }

    private Object setLocation(ProceedingJoinPoint point, VRCEventDTO<? extends EventContent> event) throws Throwable {
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
            String world_desc = WorldUtil.convertToString(world, locationMap, instance);
            embedBuilder.setDescription(MessageFormat.format("**{0}**\n{1}", description, world_desc));
        }
        return proceed;
    }

    @Around("(allOnline() || allLocation() || allUpdate() || friendAvatar() || friendDescription()|| friendOffline())  && args(event)")
    public Object setFriendLocation(ProceedingJoinPoint point, VRCEventDTO<WsFriendContent> event) throws Throwable {
        return setLocation(point, event);
    }

    @Around("(friendInvite())  && args(event)")
    public Object setNotificationLocation(ProceedingJoinPoint point, VRCEventDTO<WsNotificationContent> event) throws Throwable {
        return setLocation(point, event);
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
            StringBuilder description = embedBuilder.getDescriptionBuilder();
            List<String> lines = StringUtils.split(description.toString(), "\n", true);
            lines.set(0, "**更新了描述：**" + event.getContent().getUser().getStatusDescription());
            embedBuilder.setDescription(String.join("\n", lines));
        }
    }

    @After("friendOffline() && args(event)")
    public void setOffline(VRCEventDTO<WsFriendContent> event) {
        List<MessageDTO> messages = event.getMessages();
        for (MessageDTO message : messages) {
            EmbedBuilder embedBuilder = message.getEmbedBuilder();
            ZonedDateTime time = cacheServiceImpl.getUserOfflineTime(event.getContent().getUserId());
            embedBuilder.addField("时间", timeUtil.formatTime(time), true);
            embedBuilder.setTimestamp(time);
        }
    }

    @Around(value = "friendActive() && args(event)")
    public Object setActive(ProceedingJoinPoint point, VRCEventDTO<WsFriendContent> event) throws Throwable {
        Object proceed = point.proceed();
        String description = proceed.toString();
        logger.debug("setActive：" + description);
        List<MessageDTO> messages = event.getMessages();
        for (MessageDTO message : messages) {
            EmbedBuilder embedBuilder = message.getEmbedBuilder();
            embedBuilder.setDescription(MessageFormat.format("**{0}**", description));
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

    @After("friendInvite() && args(event)")
    public void setfriendInvite(VRCEventDTO<WsNotificationContent> event) {
        for (MessageDTO message : event.getMessages()) {
            EmbedBuilder embedBuilder = message.getEmbedBuilder();
            embedBuilder.addField("时间", timeUtil.formatTime(event.getContent().getCreated_at()), true);
        }
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
