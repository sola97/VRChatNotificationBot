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

public class ShowUserCommand extends ChannelCommand {
    private final RestTemplate restTemplate;
    private final EventWaiter waiter;

    public ShowUserCommand(EventWaiter waiter, RestTemplate restTemplate) {
        this.waiter = waiter;
        this.name = "showuser";
        this.aliases = new String[]{"showusers"};
        this.help = "`showuser [username]`    显示好友信息\n" +
                "```" +
                "    用法一：showuser lucy                  显示好友 Lucy a1b2 的当前状态\n" +
                "    用法二：showuser                       显示该Channel的所有已订阅用户的信息```\n";
        this.restTemplate = restTemplate;
    }

    @Override
    protected void execute(CommandEvent event) {
        String uri = "/rest/show/user/{channelId}/{displayName}";
        event.getChannel().sendMessage("正在查询好友 " + event.getArgs().trim() + "...").queue(msg -> {
            String argStr = event.getArgs().trim();
            Map<String, Object> urlParams = new HashMap<>();
            urlParams.put("channelId", event.getChannel().getId());
            urlParams.put("displayName", argStr);
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