package cn.sola97.vrchat.config;

import cn.sola97.vrchat.aop.listener.RedisMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;


@Configuration
public class PubsubConfig {
    @Autowired
    private RedisMessageListener redisMessageListener;


    @Bean
    public PatternTopic expiredTopic() {
        return new PatternTopic("__keyevent@*__:expired");
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        redisMessageListenerContainer.addMessageListener(redisMessageListener, expiredTopic());
        return redisMessageListenerContainer;
    }


}

