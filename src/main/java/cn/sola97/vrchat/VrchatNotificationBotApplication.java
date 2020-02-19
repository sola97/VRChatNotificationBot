package cn.sola97.vrchat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@MapperScan("cn.sola97.vrchat.mapper")
@EnableRetry
@EnableAspectJAutoProxy
public class VrchatNotificationBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(VrchatNotificationBotApplication.class, args);
    }

}
