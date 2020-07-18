package cn.sola97.vrchat.commands.channel;

import cn.sola97.vrchat.commands.ChannelCommand;
import cn.sola97.vrchat.pojo.CommandResultVO;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class SearchUserCommand extends ChannelCommand {
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
        String uri = "/rest/search/user/{channelId}/{search}";
        event.getChannel().sendMessage("正在搜索 " + event.getArgs().trim() + "...").queue(msg -> {
            String argStr = event.getArgs().trim();
            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("channelId", event.getChannel().getId());
            urlParams.put("search", argStr);
            URI URL = UriComponentsBuilder.fromUriString(uri)
                    .queryParam("callback", msg.getId())
                    .buildAndExpand(urlParams).toUri();
            CommandResultVO commandResult = restTemplate.getForObject(URL.toString(), CommandResultVO.class);
            if (commandResult != null && commandResult.getCode() != 200) {
                msg.editMessage(commandResult.getMsg()).queue();
            }
        });
    }
}