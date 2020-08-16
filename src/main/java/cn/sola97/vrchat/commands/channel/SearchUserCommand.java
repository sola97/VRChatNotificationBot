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

public class SearchUserCommand extends ChannelCommand {
    private static final Logger logger = LoggerFactory.getLogger(SearchUserCommand.class);
    private final RestTemplate restTemplate;
    private final EventWaiter waiter;

    public SearchUserCommand(EventWaiter waiter, RestTemplate restTemplate) {
        this.waiter = waiter;
        this.name = "search";
        this.aliases = new String[]{"searchuser"};
        this.help = "`search [username]`    搜索用户\n";
        this.restTemplate = restTemplate;
    }

    @Override
    protected void execute(CommandEvent event) {
        String uri = "/rest/search/user";
        logger.info("正在搜索" + event.getArgs());
        event.getChannel().sendMessage("正在搜索 " + event.getArgs().trim() + "...").queue(msg -> {
            String argStr = event.getArgs().trim();
            URI URL = UriComponentsBuilder.fromUriString(uri)
                    .queryParam("key", argStr)
                    .queryParam("channelId", event.getChannel().getId())
                    .queryParam("callback", msg.getId())
                    .build().toUri();
            CommandResultVO commandResult = restTemplate.getForObject(URL.toString(), CommandResultVO.class);
            if (commandResult != null && commandResult.getCode() != 200) {
                logger.error("SearchUserCommand URL:{} return:{}", URL.toString(), commandResult);
                msg.editMessage(commandResult.getMsg()).queue();
            }
        });
    }
}