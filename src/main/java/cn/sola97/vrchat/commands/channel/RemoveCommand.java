package cn.sola97.vrchat.commands.channel;

import cn.sola97.vrchat.commands.ChannelCommand;
import cn.sola97.vrchat.entity.Ping;
import cn.sola97.vrchat.pojo.ChannelConfigVO;
import cn.sola97.vrchat.pojo.CommandResultVO;
import cn.sola97.vrchat.pojo.SubscribeDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RemoveCommand extends ChannelCommand {
    private static final Logger logger = LoggerFactory.getLogger(RemoveCommand.class);
    private final EventWaiter waiter;
    RestTemplate restTemplate;

    public RemoveCommand(EventWaiter waiter, RestTemplate restTemplate) {
        this.waiter = waiter;
        this.name = "manage";
        this.aliases = new String[]{"remove", "rm"};
        this.help = "显示一个菜单(上限10条)，用于删除已订阅的好友";
        this.restTemplate = restTemplate;
    }

    @Override
    protected void execute(CommandEvent event) {
        displayManageMenu(event, null);
    }

    private void displayManageMenu(CommandEvent event, String resultMsg) {

        OrderedMenu.Builder builder = new OrderedMenu.Builder()
                .setEventWaiter(waiter)
                .allowTextInput(true)
                .useNumbers()
                .useCancelButton(true)
                .setTimeout(30, TimeUnit.SECONDS);

        String uri = "/rest/show/config";
        URI URL = UriComponentsBuilder.fromUriString(uri)
                .queryParam("channelId", event.getChannel().getId())
                .build().toUri();
        CommandResultVO commandResult = restTemplate.getForObject(URL.toString(), CommandResultVO.class);
        LinkedHashMap data = (LinkedHashMap) commandResult.getData();
        ObjectMapper mapper = new ObjectMapper();
        List<String> choiceText = new ArrayList<>();
        List<String> choiceData = new ArrayList<>();

        ChannelConfigVO channelConfig = mapper.convertValue(data, ChannelConfigVO.class);
        for (SubscribeDTO subscribe : channelConfig.getSubscribes()) {
            if (choiceText.size() < 10) {
                choiceText.add(subscribe.getDisplayName() + "   mask   " + subscribe.getMask() + "\n");
                choiceData.add(String.join(" ", subscribe.getUsrId(), subscribe.getChannelId()));
            }
            if (subscribe.getPings().size() > 0) {
                for (Ping ping : subscribe.getPings()) {
                    if (choiceText.size() < 10) {
                        choiceText.add(subscribe.getDisplayName() + "   " + ping.getDiscordId() + "   mask   " + ping.getMask() + "\n");
                        choiceData.add(String.join(" ", ping.getUsrId(), ping.getChannelId(), ping.getDiscordId()));
                    }
                }
            }
        }
        if (choiceText.size() == 0) {
            logger.info("当前频道还没有订阅 Channel:{}", event.getChannel().getId());
            event.reply("当前频道还没有订阅");
            return;
        }
        builder.setChoices(choiceText.toArray(new String[0]));
        builder.setCancel(msg -> {
            event.getMessage().delete().queue();
            msg.delete();
        });
        builder.setText(resultMsg);
        builder.setDescription("选择序号删除");
        builder.setSelection((msg, i) -> {
            String[] split = choiceData.get(i - 1).split("\\s+");
            String usrId = split[0];
            String channelId = split[1];
            String result = null;
            if (split.length == 2) {
                logger.info("正在删除 usrId:{} onChannel:{}", usrId, channelId);
                result = deleteSubscribe(channelId, usrId);
            } else if (split.length == 3) {
                String discordId = split[2];
                logger.info("正在删除 usrId:{} onChannel:{} @discord:{}", usrId, channelId, discordId);
                result = deletePing(channelId, usrId, discordId);
            } else {
                logger.error("选择出错 choiceData:{}", choiceData.get(i - 1));
                result = "选择出错";
            }
            displayManageMenu(event, result);
        });
        builder.build().display(event.getChannel());

    }

    private String deleteSubscribe(String channelId, String usrId) {
        String uri = "/rest/delete/subscribe";
        URI URL = UriComponentsBuilder.fromUriString(uri)
                .queryParam("channelId", channelId)
                .queryParam("usrId", usrId)
                .build().toUri();
        CommandResultVO commandResult = restTemplate.getForObject(URL.toString(), CommandResultVO.class);
        if (commandResult != null)
            return commandResult.getMsg();
        logger.error("deleteSubscribe commandResult URL:{} return null", URL.toString());
        return null;
    }

    private String deletePing(String channelId, String usrId, String discordId) {
        String uri = "/rest/delete/ping";
        URI URL = UriComponentsBuilder.fromUriString(uri)
                .queryParam("channelId", channelId)
                .queryParam("usrId", usrId)
                .queryParam("discordId", discordId)
                .build().toUri();
        CommandResultVO commandResult = restTemplate.getForObject(URL.toString(), CommandResultVO.class);
        if (commandResult != null)
            return commandResult.getMsg();
        logger.error("deletePing commandResult URL:{} return null", URL.toString());
        return null;
    }
}