package cn.sola97.vrchat.aop.aspect;

import cn.sola97.vrchat.pojo.MessageDTO;
import cn.sola97.vrchat.pojo.VRCEventDTO;
import cn.sola97.vrchat.pojo.impl.WsFriendContent;
import cn.sola97.vrchat.pojo.impl.WsNotificationContent;
import cn.sola97.vrchat.service.MessageService;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Order(1)
public class MessageAspect {
    private Logger logger = LoggerFactory.getLogger(MessageAspect.class);

    @Autowired
    private MessageService messageServiceImpl;

    @Pointcut("execution(* cn.sola97.vrchat.controller.EventHandlerMapping.notification*(..))")
    public void notification() {
    }

    @Pointcut("execution(* cn.sola97.vrchat.controller.EventHandlerMapping.friend*(..))")
    public void friendAction() {
    }

    @Pointcut("execution(* cn.sola97.vrchat.controller.EventHandlerMapping.userOnline(..)) ||" +
            "execution(* cn.sola97.vrchat.controller.EventHandlerMapping.userLocation(..)) || " +
            "execution(* cn.sola97.vrchat.controller.EventHandlerMapping.userUpdate(..))")
    public void userAction() {
    }

    @Before(value = "(friendAction()|| userAction()) && args(event)")
    public void setMessageBySubscribe(VRCEventDTO<WsFriendContent> event) {
        logger.debug("setMessageBySubscribe：" + event.getContent().toString());
        messageServiceImpl.setMessagesBySubscribe(event);
        logger.debug("fillFriendData：" + event.getContent().toString());
        messageServiceImpl.fillFriendData(event);
        for (MessageDTO message : event.getMessages()) {
            messageServiceImpl.setEmbed(event.getContent().getUser(), event.getContent().getWorld(), message);
        }
    }

    @After(value = "(friendAction()|| userAction()|| notification()) && args(event)")
    public void sendMessage(VRCEventDTO event) {
        logger.debug("message enqueue：" + event.getContent().toString() + " " + event.getMessages().toString());
        messageServiceImpl.enqueueMessages(event.getMessages());
    }

    @Before(value = "notification() && args(event)")
    public void setNotification(VRCEventDTO<WsNotificationContent> event) {
        messageServiceImpl.setMessagesBySubscribe(event);
        if (event.getMessages().isEmpty())
            messageServiceImpl.setMessageToOwner(event);
        logger.debug("fillNotificationData：" + event.getContent().toString());
        messageServiceImpl.fillNotificationData(event);
        for (MessageDTO message : event.getMessages()) {
            messageServiceImpl.setEmbed(event.getContent().getUser(), event.getContent().getWorld(), message);
        }
    }
}
