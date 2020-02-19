package cn.sola97.vrchat.commands.channel;

import cn.sola97.vrchat.commands.ChannelCommand;
import cn.sola97.vrchat.enums.EventTypeEnums;
import cn.sola97.vrchat.pojo.CommandResultVO;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.User;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AddCommand extends ChannelCommand {
    private RestTemplate restTemplate;
    private final EventWaiter waiter;
    public AddCommand(EventWaiter waiter,RestTemplate restTemplate)
    {
        this.waiter = waiter;
        this.name = "add";
        this.aliases = new String[]{"upd", "update"};
        this.help = "`add|update [username] [@discordUser] [mask 63]`         在一个Channel订阅好友\n" +
                "```假设好友名为 Lucy a1b2 （检索基于正则[\\s\\S]*keyword[\\s\\S]* 大小写不敏感）\n" +
                "    用法一：add                            获取Channel名作为好友检索的关键字\n" +
                "    用法二：add lucy                       使用lucy为好友检索的关键字\n" +
                "    用法三：add ^lucy$                     如果存在同名帐号Lucy,可在首尾加上限定符\n" +
                "    用法四：add mask 1                     使用Channel名检索，同时直接设置只显示上线消息\n" +
                "    用法五：add @届かない恋                设置消息@提醒\n" +
                "    用法六：add lucy @届かない恋 mask 1    设置该好友上线时@届かない恋\n" +
                "    PS: mask的定义可使用showconfig命令查看" +
                "```";
        this.restTemplate=restTemplate;
    }

    @Override
    protected void execute(CommandEvent event) {

        OrderedMenu.Builder builder = new OrderedMenu.Builder()
                .setEventWaiter(waiter)
                .allowTextInput(true)
                .useCancelButton(true)
                .useNumbers()
                .setTimeout(30, TimeUnit.SECONDS);
        String channelId = event.getChannel().getId();
        String uri = "/rest/add/subscribe/{channelId}/{displayName}";
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
        Map<String, Object> urlParams = new HashMap<>();
        if (args[0].equals("")) {
            args[0] = event.getChannel().getName();
        }
        urlParams.put("channelId", channelId);
        urlParams.put("displayName", args[0].trim());
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(uri)
                .queryParam("channelName", event.getChannel().getName())
                .queryParam("discordIds", discordIds.toArray())
                .queryParam("discordNames", discordNames.toArray());
        String usrId = getUsrIdByDisplayName(args[0]);
        if (discordIds.isEmpty()) {
            uriComponentsBuilder.queryParam("submask", strMask == null ? querySubscribeMask(channelId, usrId) : strMask);
        } else {
            uriComponentsBuilder.queryParam("pingmask", strMask == null ? queryPingMask(channelId, usrId, discordIds.get(0)) : strMask);
        }
        URI URL = uriComponentsBuilder.buildAndExpand(urlParams).toUri();
        event.getChannel().sendMessage("正在添加订阅...").queue(m -> {
            CommandResultVO commandResult = restTemplate.getForObject(URL.toString(), CommandResultVO.class);
            if (commandResult != null && commandResult.getCode() == 200) {

                m.editMessage("订阅成功").queue(msg -> {
                    displayMaskMenu(event, builder, channelId, usrId, commandResult.getData().toString(), discordIds, discordNames);
                });
            } else if (commandResult != null) {
                m.editMessage("订阅失败 原因：" + Optional.ofNullable(commandResult.getMsg()).orElse("") + " " + Optional.ofNullable(commandResult.getData()).orElse("")).queue();
            } else {
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
                            msg.getChannel().sendMessage("更新未完全成功 当前：" + sum + " 期望：" + discordIds.size()).queue();
                        }
                    }
                }
        );
        builder.setCancel(msg -> {
            msg.delete();
        });
        builder.build().display(event.getChannel());
    }

    private String querySubscribeMask(String channelId, String usrId) {
        String uri = "/rest/query/subscribe/mask/{channelId}/{usrId}";
        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put("channelId", channelId);
        urlParams.put("usrId", usrId);
        URI URL = UriComponentsBuilder.fromUriString(uri)
                .buildAndExpand(urlParams).toUri();
        CommandResultVO commandResult = restTemplate.getForObject(URL.toString(), CommandResultVO.class);
        if (commandResult != null && commandResult.getCode() == 200) {
            return commandResult.getData().toString();
        }
        return null;
    }

    private boolean updateSubMask(String channelId, String usrId, String mask) {
        String uri = "/rest/update/subscribe/mask/{channelId}/{usrId}";
        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put("channelId", channelId);
        urlParams.put("usrId", usrId);
        URI URL = UriComponentsBuilder.fromUriString(uri)
                .queryParam("mask", mask)
                .buildAndExpand(urlParams).toUri();
        CommandResultVO commandResult = restTemplate.getForObject(URL.toString(), CommandResultVO.class);
        return commandResult != null && commandResult.getCode() == 200;
    }


    private boolean updatePingMask(String channelId, String usrId, String discordId, String mask) {
        String uri = "/rest/update/ping/mask/{channelId}/{usrId}";
        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put("channelId", channelId);
        urlParams.put("usrId", usrId);
        urlParams.put("discordId", discordId);
        URI URL = UriComponentsBuilder.fromUriString(uri)
                .queryParam("mask", mask)
                .queryParam("discordId", discordId)
                .buildAndExpand(urlParams).toUri();
        CommandResultVO commandResult = restTemplate.getForObject(URL.toString(), CommandResultVO.class);
        return commandResult != null && commandResult.getCode() == 200;
    }

    private String queryPingMask(String channelId, String usrId, String discordId) {
        String uri = "/rest/query/ping/mask/{channelId}/{usrId}";
        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put("channelId", channelId);
        urlParams.put("usrId", usrId);
        URI URL = UriComponentsBuilder.fromUriString(uri)
                .queryParam("discordId", discordId)
                .buildAndExpand(urlParams).toUri();
        CommandResultVO commandResult = restTemplate.getForObject(URL.toString(), CommandResultVO.class);
        if (commandResult != null && commandResult.getCode() == 200) {
            return commandResult.getData().toString();
        }
        return null;
    }

    private String getUsrIdByDisplayName(String displayName) {
        String uri = "/rest/query/user/" + displayName;
        CommandResultVO commandResult = restTemplate.getForObject(uri, CommandResultVO.class);
        if (commandResult != null & commandResult.getCode() == 200) {
            return commandResult.getData().toString();
        }
        return null;
    }
}