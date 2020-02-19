package cn.sola97.vrchat.aop.aspect;

import cn.sola97.vrchat.controller.EventHandlerMapping;
import cn.sola97.vrchat.entity.User;
import cn.sola97.vrchat.entity.UserOnline;
import cn.sola97.vrchat.enums.EventTypeEnums;
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

        switch (event.getType()) {
            case ONLINE:
                if (cachedUser == null)
                    //更新缓存，然后通知
                    cacheServiceImpl.setUserOnline(user);
                else
                    return;
                break;
            case OFFLINE:
                //删除缓存
                cacheServiceImpl.setUserOffline(userId);
                break;
            case LOCATION:
                if (cachedUser == null) {
                    //改为上线通知
                    event.setType(EventTypeEnums.ONLINE);
                    eventHandlerMapping.friendOnline(event);
                    return;
                } else if (event.getContent().getLocation().equals(Optional.of(cachedUser).map(UserOnline::getLocation).orElse("")))
                    //无变化的跳过
                    return;
                else {
                    //更新缓存继续通知
                    cacheServiceImpl.setUserOnline(event.getContent().getUser());
                }
                break;
        }
        point.proceed();
    }
}
