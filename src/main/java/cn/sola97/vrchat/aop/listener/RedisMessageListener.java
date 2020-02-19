package cn.sola97.vrchat.aop.listener;

import cn.sola97.vrchat.controller.EventHandlerMapping;
import cn.sola97.vrchat.service.VRChatApiService;
import cn.sola97.vrchat.utils.VRCEventDTOFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisMessageListener implements MessageListener {
    private final static Logger logger = LoggerFactory.getLogger(RedisMessageListener.class);
    @Autowired
    VRChatApiService vrchatApiServiceImpl;
    @Autowired
    EventHandlerMapping eventHandlerMapping;
    @Value("${cache.online}")
    String onlineUserKey;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    //好友下线
    public void onMessage(Message message, byte[] pattern) {
        String key = new String(message.getBody());
        if (key.startsWith(onlineUserKey)) {
            String usrId = key.replaceAll(onlineUserKey, "");
            eventHandlerMapping.friendOffline(VRCEventDTOFactory.createOfflineEvent(usrId));
        }
    }
}