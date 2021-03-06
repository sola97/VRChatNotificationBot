package cn.sola97.vrchat.commands.channel;

import cn.sola97.vrchat.commands.ChannelCommand;
import cn.sola97.vrchat.entity.User;
import cn.sola97.vrchat.enums.TrustCorlorEnums;
import cn.sola97.vrchat.pojo.CommandResultVO;
import cn.sola97.vrchat.utils.AlignUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ShowOnlinesCommand extends ChannelCommand {
    private static final Logger logger = LoggerFactory.getLogger(ShowOnlinesCommand.class);
    private final RestTemplate restTemplate;
    private final EventWaiter waiter;

    public ShowOnlinesCommand(EventWaiter waiter, RestTemplate restTemplate) {
        this.waiter = waiter;
        this.name = "showonline";
        this.aliases = new String[]{"showonlines"};
        this.guildOnly = false;
        this.help = "显示在线好友";
        this.restTemplate = restTemplate;
    }

    @Override
    protected void execute(CommandEvent event) {
        String uri = "/rest/show/user/onlines";
        URI URL = UriComponentsBuilder.fromUriString(uri)
                .queryParam("channelId", event.getChannel().getId())
                .build().toUri();
        logger.info("正在查询在线好友 Channel:{} - {}", event.getChannel().getId(), event.getChannel().getName());
        event.getChannel().sendMessage("正在查询在线好友...").queue(msg -> {
            try {
                CommandResultVO commandResult = restTemplate.getForObject(URL.toString(), CommandResultVO.class);
                assert commandResult != null;
                if (commandResult.getCode() == 200) {
                    commandResult.getData();
                    ArrayList<LinkedHashMap> map = (ArrayList<LinkedHashMap>) commandResult.getData();
                    ObjectMapper mapper = new ObjectMapper();
                    List<User> users = mapper.convertValue(map, new TypeReference<ArrayList<User>>() {
                    });
                    List<List<User>> partition = ListUtils.partition(users, 10);
                    for (List<User> userList : partition) {
                        AlignUtil alignUtil = new AlignUtil();
                        alignUtil.setPaddingChar("-");
                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        for (User user : userList) {
                            String status = user.getStatus();
                            ArrayList<String> line = new ArrayList<>();
                            String emoji = "⚪️";
                            if (status.equals("join me")) emoji = "🔵";
                            else if (status.equals("busy")) emoji = "🔴";
                            else if (status.equals("ask me")) emoji = "\uD83D\uDFE0";
                            else if (status.equals("active")) emoji = "🟢";
                            String index = (user.getFriendIndex() == null) ? "" : "  **[[" + user.getFriendIndex() + "](https://vrchat.com/home/user/" + user.getId() + ")]**";
                            line.add(emoji + "　" + TrustCorlorEnums.getEmojiByTags(user.getTags()) + "　" + user.getDisplayName() + index);
                            line.add("　　　" + "\uD83C\uDFF7" + "　" + user.getStatusDescription());
                            alignUtil.addLine(line);
                        }
                        embedBuilder.setDescription(alignUtil.toString());
                        embedBuilder.setColor(0x2bcf5c);
                        embedBuilder.setTimestamp(Instant.now());
                        int start = partition.indexOf(userList) * 10 + 1;
                        int end = partition.indexOf(userList) * 10 + userList.size();
                        embedBuilder.setFooter(MessageFormat.format("当前在线好友{0}人 第{1}页 {2}-{3}", users.size(), partition.indexOf(userList) + 1, start, end));
                        if (partition.indexOf(userList) == 0) {
                            msg.editMessage(new MessageBuilder().setEmbed(embedBuilder.build()).setContent("查询成功").build()).queue();
                        } else {
                            msg.getChannel().sendMessage(new MessageBuilder().setEmbed(embedBuilder.build()).build()).queue();
                        }
                    }


                } else {
                    logger.error("ShowOnlinesCommand URL:{} return:{}", URL.toString(), commandResult.toString());
                    msg.editMessage("查询失败\n服务器返回错误：" + commandResult.getMsg());
                }
            } catch (Exception e) {
                logger.error("查询在线好友出错", e);
                msg.editMessage("查询出错 原因：" + e.getMessage()).queue();
            }
        });

    }
}