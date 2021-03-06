package cn.sola97.vrchat.commands.channel;

import cn.sola97.vrchat.commands.ChannelCommand;
import cn.sola97.vrchat.enums.EventTypeEnums;
import cn.sola97.vrchat.pojo.CommandResultVO;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AddCommand extends ChannelCommand {
    private static final Logger logger = LoggerFactory.getLogger(AddCommand.class);
    private RestTemplate restTemplate;
    private final EventWaiter waiter;

    public AddCommand(EventWaiter waiter, RestTemplate restTemplate) {
        this.waiter = waiter;
        this.name = "add";
        this.aliases = new String[]{"upd", "update"};
        this.help = "`add|update [username] [@discordUser] [mask 63]`         在一个Channel订阅好友\n" +
                "```假设好友名为 Lucy a1b2 （检索基于正则[\\s\\S]*keyword[\\s\\S]* 大小写不敏感）\n" +
                "    用法一：add                            默认使用Channel名作为好友检索的关键字\n" +
                "    用法二：add lucy                       使用lucy为好友检索的关键字\n" +
                "    用法三：add ^lucy$                     如果存在同名帐号Lucy,可加上匹配首尾\n" +
                "    用法四：add mask 1                     使用Channel名检索，同时直接设置只显示上线消息\n" +
                "    用法五：add @届かない恋                 设置消息@提醒\n" +
                "    用法六：add lucy @届かない恋 mask 1     设置该好友上线时@届かない恋\n" +
                "    用法七：add * mask 6                   在该Channel设置显示所有好友的上下线消息\n" +
                "    用法八：add * @届かない恋 mask 32       在该Channel设置显示所有好友的Invite、FriendRequest提醒\n" +
                "    PS: mask值的定义可使用showconfig命令查看" +
                "```";
        this.restTemplate = restTemplate;
    }

    @Override
    protected void execute(CommandEvent event) {

        OrderedMenu.Builder builder = new OrderedMenu.Builder()
                .setEventWaiter(waiter)
                .allowTextInput(true)
                .useCancelButton(true)
                .useNumbers()
                .setTimeout(60, TimeUnit.SECONDS)
                .setCancel(Message::delete);
        String channelId = event.getChannel().getId();
        String uri = "/rest/add/subscribe";
        List<String> discordNames = event.getMessage().getMentionedUsers().stream().map(User::getName).collect(Collectors.toList());
        List<String> discordIds = event.getMessage().getMentionedUsers().stream().map(IMentionable::getAsMention).collect(Collectors.toList());
        String argStr = event.getArgs().replaceAll("<.+>", "").trim();
        if (argStr.equals("")) {
            argStr = event.getChannel().getName();
        }
        String[] args = argStr.split("mask");
        String strMask = null;
        if (args.length > 1)
            strMask = args[1].split("\\s+")[1];
        if (args[0].equals("")) {
            args[0] = event.getChannel().getName();
        }


        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(uri)
                .queryParam("user", args[0].trim())
                .queryParam("channelId", channelId)
                .queryParam("channelName", event.getChannel().getName())
                .queryParam("discordIds", discordIds.toArray())
                .queryParam("discordNames", discordNames.toArray());

        String usrId = null;
        if (args[0].startsWith("usr_")) {
            usrId = args[0];
        } else {
            usrId = getUsrIdByDisplayName(args[0].trim());
        }
        if (discordIds.isEmpty()) {
            uriComponentsBuilder.queryParam("submask", strMask == null ? querySubscribeMask(channelId, usrId) : strMask);
        } else {
            uriComponentsBuilder.queryParam("pingmask", strMask == null ? queryPingMask(channelId, usrId, discordIds.get(0)) : strMask);
        }
        URI URL = uriComponentsBuilder.build().toUri();
        String finalUsrId = usrId;
        event.getChannel().sendMessage("正在添加订阅...").queue(m -> {
            CommandResultVO commandResult = restTemplate.getForObject(URL.toString(), CommandResultVO.class);
            if (commandResult != null && commandResult.getCode() == 200) {
                m.editMessage("订阅成功").queue(msg -> {
                    logger.info("订阅成功 usrId:{} onChannel:{}", finalUsrId, channelId);
                    displayMaskMenu(event, builder, channelId, finalUsrId, commandResult.getData().toString(), discordIds, discordNames);
                });
            } else if (commandResult != null) {
                logger.warn("订阅失败 URL:{} return:{}", URL.toString(), commandResult.toString());
                m.editMessage("订阅失败：" + Optional.ofNullable(commandResult.getMsg()).orElse("") + " " + Optional.ofNullable(commandResult.getData()).orElse("")).queue();
            } else {
                logger.warn("订阅失败 URL:{} return:null", URL.toString());
                m.editMessage("订阅失败").queue();
            }
        });
    }

    private void displayMaskMenu(CommandEvent event, OrderedMenu.Builder builder, String channelId, String usrId, String username, List<String> discordIds, List<String> discordNames) {
        EventTypeEnums[] eventMask = EventTypeEnums.values();
        String strMask;
        if (!discordIds.isEmpty()) {
            strMask = queryPingMask(channelId, usrId, discordIds.get(0));
            builder.setDescription(username + "\n" + String.join(" ", discordIds) + "\n\n当前订阅为:");
        } else {
            strMask = querySubscribeMask(channelId, usrId);
            builder.setDescription(username + "\n\n当前订阅为:");
        }
        assert strMask != null;
        Byte byteMask = Byte.valueOf(strMask);
        builder.clearChoices();
        for (int i = 1; i < eventMask.length; i++) {
            String choice = eventMask[i].getDescription();
            if ((eventMask[i].getMask() & byteMask) > 0)
                choice += " √";
            builder.addChoices(choice);
        }
        builder.setSelection((msg, j) -> {
                    int newMask = byteMask ^ eventMask[j].getMask();
                    if (discordIds.isEmpty()) {
                        if (updateSubMask(channelId, usrId, String.valueOf(newMask)))
                            displayMaskMenu(event, builder, channelId, usrId, username, discordIds, discordNames);
                        else {
                            msg.getChannel().sendMessage("更新失败").queue();
                        }
                    } else {
                        int sum = discordIds.stream().parallel().map(discordId -> updatePingMask(channelId, usrId, discordId, String.valueOf(newMask))).mapToInt(r -> r ? 1 : 0).sum();
                        if (sum == discordIds.size()) {
                            displayMaskMenu(event, builder, channelId, usrId, username, discordIds, discordNames);
                        } else {
                            msg.getChannel().sendMessage("更新失败 当前成功：" + sum + " 期望：" + discordIds.size()).queue();
                        }
                    }
                }
        );
        builder.build().display(event.getChannel());
    }

    private String querySubscribeMask(String channelId, String usrId) {
        String uri = "/rest/query/subscribe/mask";
        URI URL = UriComponentsBuilder.fromUriString(uri)
                .queryParam("channelId", channelId)
                .queryParam("usrId", usrId)
                .build().toUri();
        CommandResultVO commandResult = restTemplate.getForObject(URL.toString(), CommandResultVO.class);
        if (commandResult != null && commandResult.getCode() == 200) {
            return commandResult.getData().toString();
        } else {
            logger.warn("querySubscribeMask URL:{} return:{}", URL.toString(), Optional.ofNullable(commandResult));
            return null;
        }
    }

    private boolean updateSubMask(String channelId, String usrId, String mask) {
        String uri = "/rest/update/subscribe/mask";
        URI URL = UriComponentsBuilder.fromUriString(uri)
                .queryParam("mask", mask)
                .queryParam("channelId", channelId)
                .queryParam("usrId", usrId)
                .build().toUri();
        CommandResultVO commandResult = restTemplate.getForObject(URL.toString(), CommandResultVO.class);
        return commandResult != null && commandResult.getCode() == 200;
    }


    private boolean updatePingMask(String channelId, String usrId, String discordId, String mask) {
        String uri = "/rest/update/ping/mask";
        URI URL = UriComponentsBuilder.fromUriString(uri)
                .queryParam("mask", mask)
                .queryParam("channelId", channelId)
                .queryParam("usrId", usrId)
                .queryParam("discordId", discordId)
                .build().toUri();
        CommandResultVO commandResult = restTemplate.getForObject(URL.toString(), CommandResultVO.class);
        return commandResult != null && commandResult.getCode() == 200;
    }

    private String queryPingMask(String channelId, String usrId, String discordId) {
        String uri = "/rest/query/ping/mask";
        URI URL = UriComponentsBuilder.fromUriString(uri)
                .queryParam("channelId", channelId)
                .queryParam("usrId", usrId)
                .queryParam("discordId", discordId)
                .build().toUri();
        CommandResultVO commandResult = restTemplate.getForObject(URL.toString(), CommandResultVO.class);
        if (commandResult != null && commandResult.getCode() == 200) {
            return commandResult.getData().toString();
        } else {
            //没有查询到Ping记录时，为默认mask
            logger.info("queryPingMask URL:{} return:{}", URL.toString(), Optional.of(commandResult));
            return null;
        }
    }

    private String getUsrIdByDisplayName(String displayName) {
        String uri = "/rest/query/user/id";
        URI URL = UriComponentsBuilder.fromUriString(uri)
                .queryParam("displayName", displayName)
                .build().toUri();
        CommandResultVO commandResult = restTemplate.getForObject(URL.toString(), CommandResultVO.class);
        if (commandResult != null & commandResult.getCode() == 200) {
            return commandResult.getData().toString();
        } else {
            logger.error("getUsrIdByDisplayName URL:{} return:{}", uri, Optional.of(commandResult));
            return null;
        }
    }
}