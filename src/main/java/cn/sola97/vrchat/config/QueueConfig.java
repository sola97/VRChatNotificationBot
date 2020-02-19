package cn.sola97.vrchat.config;

import cn.sola97.vrchat.pojo.MessageDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Configuration
public class QueueConfig {
    @Bean
    public BlockingQueue<MessageDTO> messageBlockingQueue(){
        return new LinkedBlockingQueue<MessageDTO>();
    }
}
