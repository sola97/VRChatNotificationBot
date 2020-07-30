package cn.sola97.vrchat.aop.aspect;

import cn.sola97.vrchat.controller.EventHandlerMapping;
import cn.sola97.vrchat.entity.User;
import cn.sola97.vrchat.entity.UserOnline;
import cn.sola97.vrchat.enums.EventTypeEnums;
import cn.sola97.vrchat.pojo.EventContent;
import cn.sola97.vrchat.pojo.VRCEventDTO;
import cn.sola97.vrchat.pojo.impl.WsFriendContent;
import cn.sola97.vrchat.service.CacheService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Aspect
@Order(0)
public class CacheCheckAspect {
    private Logger logger = LoggerFactory.getLogger(CacheCheckAspect.class);

    @Autowired
    EventHandlerMapping eventHandlerMapping;
    @Autowired
    private CacheService cacheServiceImpl;

    @Pointcut("execution(* cn.sola97.vrchat.controller.EventHandlerMapping.friend*(..))")
    public void friendAction() {
    }

    @Around(value = "friendAction()&& args(event)")
    public void checkCacheAndUpdate(ProceedingJoinPoint point, VRCEventDTO<WsFriendContent> event) throws Throwable {
        String userId = event.getContent().getUserId();
        User user = event.getContent().getUser();
        UserOnline cachedUser = cacheServiceImpl.getOnlineUser(userId);
        String prefix = "";
        if (Boolean.TRUE.equals(event.getManuualCreated())) {
            prefix = "轮询-缓存检查：";
        }
        String name = prefix + Optional.ofNullable(event.getContent()).map(EventContent::getUser).map(User::getDisplayName).orElse("");
        switch (event.getType()) {
            case ACTIVE:
                if (cachedUser != null) {
                    logger.info("{}在缓存中已存在，跳过Active通知", name);
                    return;
                } else {
                    logger.info("{}在缓存中不存在，设置Active通知", name);
                }
                break;
            case ONLINE:
                if (cachedUser == null) {
                    //更新缓存，然后通知
                    logger.info("{}在缓存中不存在，设置上线通知", name);
                    cacheServiceImpl.setUserOnline(user);
                } else {
                    logger.info("{}在缓存中已存在，跳过上线通知", name);
                    return;
                }
                break;
            case OFFLINE:
                //如果是由redis创建的通知
                if (Boolean.TRUE.equals(event.getManuualCreated())) {
                    //确认没有新的缓存
                    if (cachedUser == null) {
                        //继续执行
                        logger.info("{}在缓存中已过期，设置下线通知", name);
                    } else {
                        logger.info("{}在缓存中存在，跳过下线通知", name);
                        return;
                    }
                } else {
                    //如果是websocket消息
                    //设置缓存为10秒后过期
                    if (cachedUser != null) {
                        //设置成功
                        boolean set = cacheServiceImpl.setUserOffline(userId);
                        if (set) {
                            logger.info("{}设置为下一周期下线", name);
                            return;
                        } else {
                            logger.info("{}设置为过期时间失败，可能已下线，继续下线通知", name);
                        }
                    }
                    if (cachedUser == null) {
                        logger.info("{}在缓存中已过期，设置下线通知", name);
                    }
                }
                break;
            case LOCATION:
                if (cachedUser == null) {
                    //改为上线通知
                    logger.info("{} 在缓存中不存在，设置换地图通知为上线通知", name);
                    event.setType(EventTypeEnums.ONLINE);
                    eventHandlerMapping.friendOnline(event);
                    return;
                } else if (event.getContent().getLocation().equals(Optional.of(cachedUser).map(UserOnline::getLocation).orElse(""))) {
                    //无变化的跳过
                    logger.info("{} Location没有变化，跳过换地图通知", name);
                    return;
                } else {
                    //更新缓存继续通知
                    logger.info("{} 更新缓存并发送换地图通知", name);
                    cacheServiceImpl.setUserOnline(event.getContent().getUser());
                }
                break;
        }
        point.proceed();
    }
}
