package cn.sola97.vrchat.commands.channel;

import cn.sola97.vrchat.commands.ChannelCommand;
import cn.sola97.vrchat.entity.Ping;
import cn.sola97.vrchat.enums.EventTypeEnums;
import cn.sola97.vrchat.pojo.ChannelConfigVO;
import cn.sola97.vrchat.pojo.CommandResultVO;
import cn.sola97.vrchat.pojo.SubscribeDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;

public class ShowConfigCommand extends ChannelCommand {
    private static final Logger logger = LoggerFactory.getLogger(ShowConfigCommand.class);
    private final RestTemplate restTemplate;
    private final EventWaiter waiter;

    public ShowConfigCommand(EventWaiter waiter, RestTemplate restTemplate)
    {
        this.waiter = waiter;
        this.name = "showconfig";
        this.aliases = new String[]{};
        this.help = "显示当前Channel已订阅好友的配置";
        this.restTemplate=restTemplate;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        String uri = "/rest/show/config/{channelId}";
        logger.info("正在查询Channel配置:{}", event.getChannel().getId());
        CommandResultVO commandResult = restTemplate.getForObject(uri, CommandResultVO.class,event.getChannel().getId());
        try {
            LinkedHashMap datahashmap = (LinkedHashMap) commandResult.getData();
            ObjectMapper mapper = new ObjectMapper();
            ChannelConfigVO channelConfigVO = mapper.convertValue(datahashmap, ChannelConfigVO.class);
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle(event.getChannel().getId());
            embedBuilder.setDescription(getMaskDescription());
            MessageBuilder messageBuilder = new MessageBuilder();
            for (SubscribeDTO subscribe : channelConfigVO.getSubscribes()) {
                String fieldName = subscribe.getDisplayName();
                StringBuffer fieldValue = new StringBuffer();
                if (subscribe.getPings().size() > 0) {
                    for (Ping ping : subscribe.getPings()) {
                        fieldValue.append(ping.getDiscordId()).append("    mask: ").append(ping.getMask()).append("\n");
                    }
                }
                fieldValue.append("    default: ").append(subscribe.getMask());
                embedBuilder.addField(fieldName, fieldValue.toString(), false);
            }
            messageBuilder.setEmbed(embedBuilder.build());
            event.reply(messageBuilder.build());
        }catch (Exception e){
            logger.error("ShowConfigCommand error", e);
            event.reply(commandResult.toString() + "\n" + e.getMessage());
        }
    }

    private String getMaskDescription() {
        EventTypeEnums[] eventMask = EventTypeEnums.values();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < eventMask.length; i++) {
            buffer.append(eventMask[i].getMask()).append(" - ").append(eventMask[i].getDescription()).append("\n");
        }
        return buffer.toString();
    }

    protected String toBinaryString(int i) {
        return String.format("%5s", Integer.toBinaryString(i)).replace(" ", "0");
    }
}
