package cn.sola97.vrchat.commands.channel;

import cn.sola97.vrchat.commands.ChannelCommand;
import cn.sola97.vrchat.pojo.CommandResultVO;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

public class JoinUserCommand extends ChannelCommand {
    private static final Logger logger = LoggerFactory.getLogger(ShowUserCommand.class);
    private final RestTemplate restTemplate;
    private final EventWaiter waiter;

    public JoinUserCommand(EventWaiter waiter, RestTemplate restTemplate) {
        this.waiter = waiter;
        this.name = "join";
        this.aliases = new String[]{"inviteme"};
        this.help = "`join [username|user_id]`    显示好友信息\n" +
                "```" +
                "    用法一：join lucy                      join好友 Lucy a1b2\n" +
                "    用法二：join                           join该Channel的在线用户\n" +
                "    用法三：join usr_id                    join该指定ID的用户```\n";
        this.restTemplate = restTemplate;
    }

    @Override
    public void execute(CommandEvent event) {
        String param = event.getArgs().trim();
        String uri = "/rest/join/user";
        String message = "正在给自己发送邀请到  " + param + "...";

        logger.info("{} Channel:{} - {}", message, event.getChannel().getId(), event.getChannel().getName());
        event.getChannel().sendMessage(message).queue(msg -> {
            String displayName = "";
            String userId = "";
            if (param.startsWith("usr_")) {
                userId = param;
            } else {
                displayName = param;
            }
            URI URL = UriComponentsBuilder.fromUriString(uri)
                    .queryParam("userId", userId)
                    .queryParam("displayName", displayName)
                    .queryParam("channelId", event.getChannel().getId())
                    .queryParam("callback", msg.getId())
                    .build().toUri();
            CommandResultVO commandResult = restTemplate.getForObject(URL.toString(), CommandResultVO.class);
            if (commandResult != null && commandResult.getCode() != 200) {
                logger.error("JoinUserCommand URL:{}  return:{}", URL.toString(), commandResult);
                msg.editMessage(commandResult.getMsg()).queue();
            }
        });
    }
}