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

public class ShowUserCommand extends ChannelCommand {
    private static final Logger logger = LoggerFactory.getLogger(ShowUserCommand.class);
    private final RestTemplate restTemplate;
    private final EventWaiter waiter;

    public ShowUserCommand(EventWaiter waiter, RestTemplate restTemplate) {
        this.waiter = waiter;
        this.name = "showuser";
        this.aliases = new String[]{"showusers"};
        this.help = "`showuser [username|user_id]`    显示好友信息\n" +
                "```" +
                "    用法一：showuser lucy                  显示好友 Lucy a1b2 的当前状态\n" +
                "    用法二：showuser                       显示该Channel的所有已订阅用户的信息\n" +
                "    用法三：showuser usr_id                显示该指定ID的用户信息```\n";
        this.restTemplate = restTemplate;
    }

    @Override
    protected void execute(CommandEvent event) {
        String param = event.getArgs().trim();
        String uri = "/rest/show/user";
        String message = "正在查询  " + param + "...";

        logger.info("{} on Channel:{} - {}", message, event.getChannel().getId(), event.getChannel().getName());
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
                logger.error("ShowUserCommand URL:{}  return:{}", URL.toString(), commandResult);
                msg.editMessage(commandResult.getMsg()).queue();
            }
        });
    }
}