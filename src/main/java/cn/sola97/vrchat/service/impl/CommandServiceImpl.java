package cn.sola97.vrchat.service.impl;

import cn.sola97.vrchat.aop.proxy.JDAProxy;
import cn.sola97.vrchat.aop.proxy.WebSocketConnectionManagerProxy;
import cn.sola97.vrchat.entity.*;
import cn.sola97.vrchat.enums.EventTypeEnums;
import cn.sola97.vrchat.enums.TrustCorlorEnums;
import cn.sola97.vrchat.enums.WorldInstanceEnums;
import cn.sola97.vrchat.pojo.ChannelConfigVO;
import cn.sola97.vrchat.pojo.CommandResultVO;
import cn.sola97.vrchat.pojo.MessageDTO;
import cn.sola97.vrchat.pojo.SubscribeDTO;
import cn.sola97.vrchat.service.*;
import cn.sola97.vrchat.utils.WorldUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Component
public class CommandServiceImpl implements CommandService {
    private static final Logger logger = LoggerFactory.getLogger(CommandServiceImpl.class);
    @Autowired
    SubscribeService subscribeServiceImpl;
    @Autowired
    ChannelService channelServiceImpl;
    @Autowired
    VRChatApiService vrchatApiServiceImpl;
    @Autowired
    MessageService messageServiceImpl;
    @Autowired
    PingService pingServiceImpl;
    @Autowired
    WebSocketConnectionManagerProxy webSocketConnectionManagerProxy;
    @Resource
    CommandService commandServiceImpl;
    @Autowired
    JDAProxy jdaProxy;
    @Autowired
    @Qualifier("asyncExecutor")
    Executor asyncExecutor;
    @Value("${timezone}")
    String timezone;
    @Value("${discord.channel.default-mask}")
    String defaultMask;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommandResultVO subscribe(@NotNull String userKey, @NotNull String channelId, @NotNull String channelName, @NotNull List<String> discordIds, @NotNull List<String> discordNames, @NotNull Byte subMask, @NotNull Byte pingMask) throws Exception {
        int maxMask = Arrays.stream(EventTypeEnums.values()).mapToInt(EventTypeEnums::getMask).sum();
        if (subMask > maxMask || pingMask > maxMask || subMask < 0 || pingMask < 0)
            return new CommandResultVO().setCode(400).setMsg("mask超出范围，mask应在 0-" + maxMask + "之间");
        User user = null;
        List<User> users = vrchatApiServiceImpl.getUserByNameOrId(userKey, null);
        if (users.size() == 1) {
            user = users.get(0);
            if (user.getId() == null) {
                return new CommandResultVO().setCode(500).setMsg("没有获取到该用户的数据");
            }
        } else if (users.size() > 1) {
            return new CommandResultVO().setCode(400).setMsg(userKey + "有多个对应用户").setData(users.stream().map(User::getDisplayName).collect(Collectors.joining("、")));
        } else {
            return new CommandResultVO().setCode(404).setMsg("找不到" + userKey + "对应用户");
        }

        int index = 0;
        index += 1;
        //channel
        Channel channel = new Channel();
        channel.setChannelId(channelId);
        channel.setChannelName(channelName);
        index -= channelServiceImpl.insChannelElseUpd(channel);

        //更新subscribe
        index += 1;
        Subscribe subscribe = new Subscribe();
        subscribe.setChannelId(channelId);
        subscribe.setUsrId(user.getId());
        subscribe.setDisplayName(user.getDisplayName());
        subscribe.setMask(subMask);
        if (discordIds.isEmpty()) {
            //不更新Ping时update subscribe
            index -= subscribeServiceImpl.insSubscribeElseUpd(subscribe);
        } else {
            //不update，只插入
            index -= subscribeServiceImpl.insSubscribeIfNotExists(subscribe);
        }

        //更新ping
        index += discordIds.size();
        for (int i = 0; i < discordIds.size(); i++) {
            Ping ping = new Ping();
            ping.setUsrId(user.getId());
            ping.setChannelId(channelId);
            ping.setDiscordId(discordIds.get(i));
            ping.setDiscordName(discordNames.get(i));
            ping.setMask(pingMask);
            index -= pingServiceImpl.insPingElseUpd(ping);
        }

        if (index == 0) {
            return new CommandResultVO().setCode(200).setMsg("订阅成功").setData(user.getDisplayName());
        }
        throw new Exception("创建订阅失败");
    }


    @Override
    public CommandResultVO showConfigByChannelId(String channelId) {
        Channel channel = channelServiceImpl.selChannelById(channelId);
        List<Subscribe> subscribes = subscribeServiceImpl.selSubscribesByChannelId(channelId);
        ChannelConfigVO channelConfigVO = new ChannelConfigVO();
        List<SubscribeDTO> subscribeChildren = new ArrayList<>();
        for (Subscribe subscribe : subscribes) {
            SubscribeDTO child = new SubscribeDTO();
            child.setUsrId(subscribe.getUsrId());
            child.setChannelId(subscribe.getChannelId());
            child.setDisplayName(subscribe.getDisplayName());
            child.setMask(subscribe.getMask());
            List<Ping> pings = pingServiceImpl.selPingByChannelIdAndUserId(subscribe.getChannelId(), subscribe.getUsrId());
            child.setPings(pings);
            subscribeChildren.add(child);
        }
        channelConfigVO.setSubscribes(subscribeChildren);
        return new CommandResultVO().setCode(200).setMsg("查询成功").setData(channelConfigVO);
    }

    @Override
    public CommandResultVO showUserByChannelId(String channelId, String callback) {
        CompletableFuture.supplyAsync(() -> subscribeServiceImpl.selSubscribesByChannelId(channelId), asyncExecutor)
                .thenApply(subscribeList -> {
                    List<CompletableFuture<MessageDTO>> futures = new ArrayList<>();
                    for (Subscribe subscribe : subscribeList) {
                        futures.add(CompletableFuture.supplyAsync(() -> commandServiceImpl.showUser(subscribe.getUsrId(), channelId, null), asyncExecutor));
                    }
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).exceptionally(ex -> {
                        logger.error("error on showuser:" + ex.getMessage());
                        return null;
                    }).join();
                    return futures;
                }).thenAccept((futures) -> {
            Map<Boolean, List<CompletableFuture<MessageDTO>>> result = futures.stream().collect(Collectors.partitioningBy(CompletableFuture::isCompletedExceptionally));
            ArrayList<MessageDTO> messages = new ArrayList<>();
            for (CompletableFuture<MessageDTO> completableFuture : result.get(Boolean.FALSE)) {
                try {
                    messages.add(completableFuture.get());
                } catch (Exception e) {
                    logger.error("completableFuture.get message failed." + e.getMessage());
                }
            }
            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setType(MessageDTO.typeEnums.EDIT_MESSAGE);
            messageDTO.setCallback(callback);
            messages.removeIf(Objects::isNull);
            messageDTO.setContent("查询成功 Total:" + messages.size());
            messageDTO.setChannelId(channelId);
            messages.add(0, messageDTO);
            messageServiceImpl.enqueueMessages(messages);
        });
        return new CommandResultVO()
                .setCode(200).setMsg("正在查询...");
    }

    @Override
    public CommandResultVO showUserByName(String displayName, String channelId, String callback) {
        CompletableFuture.supplyAsync(() -> vrchatApiServiceImpl.getUserByNameOrId(displayName, null), asyncExecutor)
                .thenApply(users -> {
                    List<CompletableFuture<MessageDTO>> futures = new ArrayList<>();
                    for (User user : users) {
                        futures.add(CompletableFuture.supplyAsync(() -> commandServiceImpl.showUser(user.getId(), channelId, null), asyncExecutor));
                    }
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).exceptionally(ex -> {
                        logger.error("error on showuser:" + ex.getMessage());
                        return null;
                    }).join();
                    return futures;
                })
                .thenAccept((futures) -> {
                    Map<Boolean, List<CompletableFuture<MessageDTO>>> result = futures.stream().collect(Collectors.partitioningBy(CompletableFuture::isCompletedExceptionally));
                    ArrayList<MessageDTO> messages = new ArrayList<>();
                    for (CompletableFuture<MessageDTO> completableFuture : result.get(Boolean.FALSE)) {
                        try {
                            messages.add(completableFuture.get());
                        } catch (Exception e) {
                            logger.error("completableFuture.get message failed." + e.getMessage());
                        }
                    }
                    MessageDTO messageDTO = new MessageDTO();
                    messageDTO.setType(MessageDTO.typeEnums.EDIT_MESSAGE);
                    messageDTO.setCallback(callback);
                    messages.removeIf(Objects::isNull);
                    messageDTO.setContent("查询成功 Total:" + messages.size());
                    messageDTO.setChannelId(channelId);
                    messages.add(0, messageDTO);
                    messageServiceImpl.enqueueMessages(messages);
                });
        return new CommandResultVO().setCode(200).setMsg("正在查询...");
    }

    @Override
    public CommandResultVO getPingMask(String channelId, String usrId, String discordId) {
        Ping ping = new Ping();
        ping.setUsrId(usrId);
        ping.setChannelId(channelId);
        ping.setDiscordId(discordId);
        Ping res = pingServiceImpl.selPingByPrimaryKey(ping);
        if (res != null)
            return new CommandResultVO().setCode(200).setMsg("OK").setData(res.getMask().intValue());
        else
            return new CommandResultVO().setCode(404).setData("没有找到Ping记录 channelId:" + channelId + " usrId:" + usrId + "discordId:" + discordId);
    }

    @Override
    public CommandResultVO updatePingMask(String channelId, String usrId, String discordId, String mask) {
        Ping ping = new Ping();
        ping.setUsrId(usrId);
        ping.setChannelId(channelId);
        ping.setDiscordId(discordId);
        ping.setMask(Byte.valueOf(mask));
        int i = pingServiceImpl.updPing(ping);
        if (i > 0)
            return new CommandResultVO().setCode(200).setMsg("更新成功").setData(i);
        else
            return new CommandResultVO().setCode(500).setMsg("更新失败");
    }

    @Override
    public CommandResultVO getSubscribeMask(String channelId, String usrId) {

        Subscribe subscribe = new Subscribe();
        subscribe.setUsrId(usrId);
        subscribe.setChannelId(channelId);
        Subscribe res = subscribeServiceImpl.selSubscribesByPrimaryKey(subscribe);
        if (res != null)
            return new CommandResultVO().setCode(200).setMsg("OK").setData(res.getMask().intValue());
        else
            return new CommandResultVO().setCode(404).setData("没有找到Subscribe记录 channelId:" + channelId + " usrId:" + usrId);
    }

    @Override
    public CommandResultVO updateSubscribeMask(String channelId, String usrId, String mask) {
        Subscribe subscribe = new Subscribe();
        subscribe.setUsrId(usrId);
        subscribe.setChannelId(channelId);
        subscribe.setMask(Byte.valueOf(mask));
        int i = subscribeServiceImpl.updSubscribe(subscribe);
        if (i > 0)
            return new CommandResultVO().setCode(200).setMsg("更新成功").setData(i);
        else
            return new CommandResultVO().setCode(500).setMsg("更新失败");
    }


    @Override
    public CommandResultVO getUserIdByName(String displayName) {
        if (StringUtils.isEmpty(displayName)) {
            return new CommandResultVO().setCode(400).setMsg("传入参数为空");
        }
        List<User> users = vrchatApiServiceImpl.getUserByNameOrId(displayName, null);
        if (users.size() == 1) {
            return new CommandResultVO().setCode(200).setMsg("OK").setData(users.get(0).getId());
        } else if (users.size() > 1) {
            return new CommandResultVO().setCode(400).setMsg(displayName + "有多个对应用户").setData(users.stream().map(User::getDisplayName).collect(Collectors.joining("、")));
        } else {
            return new CommandResultVO().setCode(404).setMsg("找不到" + displayName + "对应用户");
        }
    }

    @Override
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public MessageDTO showUser(String usrId, String channelId, String callback) {
        if (usrId.equals("*")) return null;
        MessageDTO message = new MessageDTO();
        message.setType(MessageDTO.typeEnums.SEND_TO_CHANNEL);
        message.setChannelId(channelId);
        if (callback != null) {
            message.setType(MessageDTO.typeEnums.EDIT_MESSAGE);
            message.setCallback(callback);
            message.setContent("查询成功");
        }
        User user = vrchatApiServiceImpl.getUserById(usrId, true);
        World world = vrchatApiServiceImpl.getWorldById(user.getWorldId(), true);
        messageServiceImpl.setEmbed(user, world, message);
        EmbedBuilder embedBuilder = message.getEmbedBuilder();
        String description = "当前状态";
        Map<String, String> locationMap = message.getLocationMap();
        String instance = user.getInstanceId();
        String world_desc = WorldUtil.convertToString(world, locationMap, instance);
        embedBuilder.setDescription(MessageFormat.format("**{0}**\n{1}", description, world_desc));
        StringBuilder accountInfo = new StringBuilder();
        accountInfo.append("username：").append(user.getUsername()).append("\n");
        accountInfo.append("用户ID　：").append(user.getId());
        if (user.getLast_login() != null) {
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
                accountInfo.append("\n");
                accountInfo.append("上次登录：").append(simpleDateFormat.format(user.getLast_login()));
            } catch (Exception e) {
                logger.error("格式化上次登录时间出错 getLast_login():" + user.getLast_login(), e);
            }
        }
        embedBuilder.addField("帐号信息", accountInfo.toString(), false);
        return message;
    }

    @Override
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public MessageDTO joinUser(String usrId, String channelId, String callback) {
        if (usrId.equals("*") || StringUtils.isEmpty(usrId)) return null;
        MessageDTO message = new MessageDTO();
        message.setType(MessageDTO.typeEnums.SEND_TO_CHANNEL);
        message.setChannelId(channelId);
        if (callback != null) {
            message.setType(MessageDTO.typeEnums.EDIT_MESSAGE);
            message.setCallback(callback);
        }
        //获取用户信息
        User user = vrchatApiServiceImpl.getUserById(usrId, false);
        String location = user.getLocation();
        Map<String, String> map = WorldInstanceEnums.parseLocation(location);
        //没有location信息，无法Join
        if (!map.containsKey("location")) {
            if (map.containsKey("worldId")) {
                if (map.get("worldId").equals("offline")) {
                    message.setContent(MessageFormat.format("好友 **{0}** 当前不在线", user.getDisplayName()));
                } else if (map.get("worldId").equals("private")) {
                    message.setContent(MessageFormat.format("好友 **{0}** 当前在Private", user.getDisplayName()));
                }
            } else if (user instanceof CurrentUser) {
                message.setContent("无法邀请自己");
            } else {
                message.setContent(MessageFormat.format("没有Location数据{}", user.getDisplayName()));
            }
            return message;
        }
        //调用API邀请
        VRCResponse vrcResponse = vrchatApiServiceImpl.joinLocation(map.get("location"));
        if (vrcResponse.getSuccess() != null && vrcResponse.getSuccess().getStatus_code().equals(200)) {
            World world = vrchatApiServiceImpl.getWorldById(user.getWorldId(), true);
            messageServiceImpl.setEmbed(user, world, message);
            EmbedBuilder embedBuilder = message.getEmbedBuilder();
            String description = "创建邀请成功";
            Map<String, String> locationMap = message.getLocationMap();
            String instance = user.getInstanceId();
            String world_desc = WorldUtil.convertToString(world, locationMap, instance);
            embedBuilder.setDescription(MessageFormat.format("**{0}**\n{1}", description, world_desc));
            return message;
        } else {
            message.setContent(MessageFormat.format("创建邀请失败 error:{0} status_code:{}", vrcResponse.getError(), vrcResponse.getStatus_code()));
            logger.warn("创建邀请失败 error:{} status_code:{} user:{}", vrcResponse, vrcResponse.getStatus_code(), user);
            return message;
        }
    }


    @Override
    public CommandResultVO deleteSubscribe(String channelId, String usrId) {
        Subscribe subscribe = new Subscribe();
        subscribe.setUsrId(usrId);
        subscribe.setChannelId(channelId);
        int i = subscribeServiceImpl.delSubscribe(subscribe);
        if (i > 0) {
            Ping ping = new Ping();
            ping.setChannelId(channelId);
            ping.setUsrId(usrId);
            i += pingServiceImpl.delPingByChannelIdAndUsrId(ping);
            return new CommandResultVO().setCode(200).setMsg("成功删除" + i + "条记录");
        }
        return new CommandResultVO().setCode(404).setData("没有找到该记录");
    }

    @Override
    public CommandResultVO deletePing(String channelId, String usrId, String discordId) {
        Ping ping = new Ping();
        ping.setChannelId(channelId);
        ping.setUsrId(usrId);
        ping.setDiscordId(discordId);
        int i = pingServiceImpl.delPingByPrimaryKey(ping);
        if (i > 0) {
            return new CommandResultVO().setCode(200).setMsg("成功删除" + i + "条记录");
        }
        return new CommandResultVO().setCode(404).setData("没有找到该记录");
    }

    @Override
    public CommandResultVO getOnlineUsers(String channelId) {
        List<UserOnline> friends = vrchatApiServiceImpl.getFriends(false);
        return new CommandResultVO().setCode(200).setMsg("查询成功").setData(friends);
    }

    @Override
    public CommandResultVO searchUsers(String channelId, String searchKey, String callback) {
        CompletableFuture.supplyAsync(() -> vrchatApiServiceImpl.searchUser(searchKey, 50, 0), asyncExecutor)
                .thenAccept(users -> {
                    List<User> matchUsers = users.stream()
                            .filter(user -> user.getDisplayName().matches("(?i:[\\s\\S]*" + searchKey + "[\\s\\S]*)"))
                            .collect(Collectors.toList());
                    MessageDTO messageDTO = new MessageDTO();
                    messageDTO.setType(MessageDTO.typeEnums.EDIT_MESSAGE);
                    messageDTO.setCallback(callback);
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    messageDTO.setChannelId(channelId);
                    if (!matchUsers.isEmpty()) {
                        messageDTO.setContent("找到以下" + users.size() + "个用户");
                        matchUsers.forEach(user -> embedBuilder.addField(TrustCorlorEnums.getEmojiByTags(user.getTags()) + "　" + user.getDisplayName(), user.getId(), false));
                        messageDTO.setEmbedBuilder(embedBuilder);
                        messageServiceImpl.enqueueMessages(Collections.singletonList(messageDTO));
                    } else {
                        messageDTO.setContent("没有找到匹配用户，搜索结果如下：");
                        users.forEach(user -> embedBuilder.addField(TrustCorlorEnums.getEmojiByTags(user.getTags()) + " " + user.getDisplayName(), user.getId(), false));
                        messageDTO.setEmbedBuilder(embedBuilder);
                        messageServiceImpl.enqueueMessages(Collections.singletonList(messageDTO));
                    }
                });
        return new CommandResultVO().setCode(200).setMsg("正在搜索...");
    }

    @Override
    public CommandResultVO reconnectWebsocket() {
        webSocketConnectionManagerProxy.rebuild();
        return new CommandResultVO().setCode(200).setMsg("正在重新连接VRChat Websocket");
    }

    @Override
    public CommandResultVO restartBot() {
        jdaProxy.rebuild();
        return new CommandResultVO().setCode(200).setMsg("正在重启Bot");
    }

    @Override
    public CommandResultVO getWebsocketStatus() {
        if (webSocketConnectionManagerProxy.isConnected()) {
            return new CommandResultVO().setCode(200).setMsg("VRChat Websocket is Connected.");
        }
        return new CommandResultVO().setCode(200).setMsg("VRChat Websocket Disconnected.");
    }

    @Override
    public CommandResultVO getBotStatus() {
        return new CommandResultVO().setCode(200).setMsg("Discord Bot is " + jdaProxy.getStatus().name());
    }

    @Override
    public CommandResultVO showUserById(String userId, String channelId, String callback) {
        CompletableFuture.supplyAsync(() -> showUser(userId, channelId, callback), asyncExecutor)
                .thenAccept(messageDTO ->
                        messageServiceImpl.enqueueMessages(Collections.singletonList(messageDTO)));
        return new CommandResultVO().setCode(200).setMsg("正在查询...");
    }

    @Override
    public CommandResultVO joinUserById(String userId, String channelId, String callback) {
        CompletableFuture.supplyAsync(() -> joinUser(userId, channelId, callback), asyncExecutor)
                .thenAccept(messageDTO ->
                        messageServiceImpl.enqueueMessages(Collections.singletonList(messageDTO)));
        return new CommandResultVO().setCode(200).setMsg("正在创建邀请...");
    }

    @Override
    public CommandResultVO joinUserByName(String displayName, String channelId, String callback) {
        CompletableFuture.supplyAsync(() -> vrchatApiServiceImpl.getUserByNameOrId(displayName, true), asyncExecutor) //获取在线好友
                .thenApply(users -> {
                    List<CompletableFuture<MessageDTO>> futures = new ArrayList<>();
                    for (User user : users) {
                        futures.add(CompletableFuture.supplyAsync(() -> commandServiceImpl.joinUser(user.getId(), channelId, null), asyncExecutor));
                    }
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).exceptionally(ex -> {
                        logger.error("error on joinUser:" + ex.getMessage());
                        return null;
                    }).join();
                    return futures;
                })
                .thenAccept((futures) -> {
                    Map<Boolean, List<CompletableFuture<MessageDTO>>> result = futures.stream().collect(Collectors.partitioningBy(CompletableFuture::isCompletedExceptionally));
                    ArrayList<MessageDTO> messages = new ArrayList<>();
                    for (CompletableFuture<MessageDTO> completableFuture : result.get(Boolean.FALSE)) {
                        try {
                            messages.add(completableFuture.get());
                        } catch (Exception e) {
                            logger.error("completableFuture.get message failed." + e.getMessage());
                        }
                    }
                    MessageDTO messageDTO = new MessageDTO();
                    messageDTO.setType(MessageDTO.typeEnums.EDIT_MESSAGE);
                    messageDTO.setCallback(callback);
                    messages.removeIf(Objects::isNull);
                    messageDTO.setContent("邀请结果 Total:" + messages.size());
                    messageDTO.setChannelId(channelId);
                    messages.add(0, messageDTO);
                    messageServiceImpl.enqueueMessages(messages);
                });

        return new CommandResultVO().setCode(200).setMsg("正在创建邀请...");
    }

    @Override
    public CommandResultVO joinUserByChannelId(String channelId, String callback) {
        CompletableFuture.supplyAsync(() -> subscribeServiceImpl.selSubscribesByChannelId(channelId), asyncExecutor)
                .thenApply(subscribeList -> {
                    List<CompletableFuture<MessageDTO>> futures = new ArrayList<>();
                    for (Subscribe subscribe : subscribeList) {
                        futures.add(CompletableFuture.supplyAsync(() -> commandServiceImpl.joinUser(subscribe.getUsrId(), channelId, null), asyncExecutor));
                    }
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).exceptionally(ex -> {
                        logger.error("error on joinUser:" + ex.getMessage());
                        return null;
                    }).join();
                    return futures;
                }).thenAccept((futures) -> {
            Map<Boolean, List<CompletableFuture<MessageDTO>>> result = futures.stream().collect(Collectors.partitioningBy(CompletableFuture::isCompletedExceptionally));
            ArrayList<MessageDTO> messages = new ArrayList<>();
            for (CompletableFuture<MessageDTO> completableFuture : result.get(Boolean.FALSE)) {
                try {
                    messages.add(completableFuture.get());
                } catch (Exception e) {
                    logger.error("completableFuture.get message failed." + e.getMessage());
                }
            }
            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setType(MessageDTO.typeEnums.EDIT_MESSAGE);
            messageDTO.setCallback(callback);
            messages.removeIf(Objects::isNull);
            messageDTO.setContent("邀请结果 Total:" + messages.size());
            messageDTO.setChannelId(channelId);
            messages.add(0, messageDTO);
            messageServiceImpl.enqueueMessages(messages);
        });
        return new CommandResultVO()
                .setCode(200).setMsg("正在创建邀请...");
    }

    @Override
    public CommandResultVO joinLaunchURL(String channelId, String worldId, String instanceId, String callback) {
        String location = worldId + ":" + instanceId;
        //调用API邀请
        VRCResponse vrcResponse = vrchatApiServiceImpl.joinLocation(location);
        if (vrcResponse.getSuccess() != null && vrcResponse.getSuccess().getStatus_code().equals(200)) {
            logger.info("创建邀请成功 Launch: {}", location);
            return new CommandResultVO().setMsg("创建邀请成功").setCode(200);
        } else {
            logger.warn("创建邀请失败 error:{} code:{}", vrcResponse, vrcResponse.getStatus_code());
            return new CommandResultVO().setMsg("创建邀请失败").setCode(Integer.valueOf(vrcResponse.getStatus_code()));
        }
    }
}
