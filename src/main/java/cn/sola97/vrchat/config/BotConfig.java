package cn.sola97.vrchat.config;

import cn.sola97.vrchat.aop.listener.BotListenerAdapter;
import cn.sola97.vrchat.aop.proxy.JDAProxy;
import cn.sola97.vrchat.commands.channel.*;
import cn.sola97.vrchat.commands.owner.RestartCommand;
import cn.sola97.vrchat.commands.owner.ShowIPCommand;
import cn.sola97.vrchat.utils.ProxyUtil;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.net.Proxy;


@Configuration
public class BotConfig {
    @Value("${bot.ownerId}")
    String ownerId;
    @Value("${bot.prefix}")
    String prefix;
    @Value("${bot.token}")
    String token;
    @Value("${bot.proxy:}")
    String proxyString;

    @Bean
    JDAProxy jdaProxy(JDABuilder jdaBuilder) {
        return new JDAProxy(jdaBuilder);
    }

    @Bean
    public JDABuilder jdaBuilder(EventWaiter eventWaiter, CommandClient client, BotListenerAdapter botListenerAdapter) {
        Proxy proxy = ProxyUtil.getProxy(proxyString);
        JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT)
                .setToken(token)
                .setActivity(Activity.playing("loading..."))
                .addEventListeners(eventWaiter, client, botListenerAdapter)
                .setAutoReconnect(true)
                .setMaxReconnectDelay(32);
        if (proxy != null) {
            jdaBuilder.setHttpClientBuilder(new OkHttpClient.Builder().proxy(proxy));
        }
        return jdaBuilder;
    }

    @Bean
    public CommandClient client(EventWaiter eventWaiter, RestTemplate cmdRestTemplate) {
        CommandClientBuilder clientBuilder = new CommandClientBuilder();
        clientBuilder.useDefaultGame()
                .setOwnerId(ownerId)
                .setPrefix(prefix)
                .addCommands(
                        new AddCommand(eventWaiter, cmdRestTemplate),
                        new RemoveCommand(eventWaiter, cmdRestTemplate),
                        new ShowConfigCommand(eventWaiter, cmdRestTemplate),
                        new ShowUserCommand(eventWaiter, cmdRestTemplate),
                        new ShowOnlinesCommand(eventWaiter, cmdRestTemplate),
                        new SearchUserCommand(eventWaiter, cmdRestTemplate),
                        new JoinUserCommand(eventWaiter, cmdRestTemplate),
                        new ShowIPCommand(),
                        new RestartCommand(cmdRestTemplate)
                );
        return clientBuilder.build();
    }

    @Bean
    public EventWaiter waiter() {
        return new EventWaiter();
    }
}
