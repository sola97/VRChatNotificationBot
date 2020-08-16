package cn.sola97.vrchat.controller;

import cn.sola97.vrchat.pojo.CommandResultVO;
import cn.sola97.vrchat.service.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

@Controller
@ResponseBody
public class CommandsController {
    private static final Logger logger = LoggerFactory.getLogger(CommandsController.class);
    @Autowired
    CommandService commandServiceImpl;
    @Autowired
    ChannelService channelServiceImpl;
    @Autowired
    SubscribeService subscribeServiceImpl;
    @Autowired
    PingService pingServiceImpl;
    @Autowired
    MessageService messageServiceImpl;
    @Autowired
    VRChatApiService vrchatApiServiceImpl;
    @Value("${discord.channel.default-mask}")
    String defaultSubMask;
    @Value("${discord.channel.default-ping-mask}")
    String defaultPingMask;

    @RequestMapping("/")
    public CommandResultVO welcome() {
        return new CommandResultVO().setCode(200).setMsg("It works!");
    }

    @RequestMapping("/rest/add/subscribe")
    public CommandResultVO addSubscribe(@RequestParam String user,
                                        @RequestParam String channelId,
                                        @RequestParam String channelName,
                                        @RequestParam(defaultValue = "") List<String> discordIds,
                                        @RequestParam(defaultValue = "") List<String> discordNames,
                                        @RequestParam(name = "pingmask", required = false) String pingMask,
                                        @RequestParam(name = "submask", required = false) String subMask) {
        try {
            Byte byteSubMask = Byte.valueOf(defaultSubMask);
            Byte bytePingMask = Byte.valueOf(defaultPingMask);
            if (StringUtils.isNotEmpty(pingMask)) bytePingMask = Byte.valueOf(pingMask);
            if (StringUtils.isNotEmpty(subMask)) byteSubMask = Byte.valueOf(subMask);

            String userKey = URLDecoder.decode(user, "utf8");
            List<String> decodedDiscordIds = new ArrayList<>();
            for (String discordId : discordIds) {
                decodedDiscordIds.add(URLDecoder.decode(discordId, "utf8"));
            }
            return commandServiceImpl.subscribe(userKey, channelId, channelName, decodedDiscordIds, discordNames, byteSubMask, bytePingMask);
        } catch (Exception e) {
            logger.error("addSubscribe error", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("/rest/delete/subscribe")
    public CommandResultVO deleteSubscribe(@RequestParam String channelId, @RequestParam String usrId) {
        try {
            return commandServiceImpl.deleteSubscribe(channelId, usrId);
        } catch (Exception e) {
            logger.error("deleteSubscribe error", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("/rest/delete/ping")
    public CommandResultVO deleteSubscribe(@RequestParam String channelId, @RequestParam String usrId, @RequestParam String discordId) {
        try {
            String decoded = URLDecoder.decode(discordId, "UTF-8");
            return commandServiceImpl.deletePing(channelId, usrId, decoded);
        } catch (Exception e) {
            logger.error("deleteSubscribe error", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }


    @RequestMapping("/rest/show/config")
    public CommandResultVO showChannelConfig(@RequestParam String channelId) {
        try {
            return commandServiceImpl.showConfigByChannelId(channelId);
        } catch (Exception e) {
            logger.error("showChannelConfig error", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("/rest/show/user")
    public CommandResultVO showChannelUsers(@RequestParam String channelId, @RequestParam(required = false) String displayName, @RequestParam(required = false) String userId, @RequestParam String callback) {
        try {
            if (StringUtils.isNotEmpty(userId)) {
                return commandServiceImpl.showUserById(userId, channelId, callback);
            }
            if (StringUtils.isNotEmpty(displayName)) {
                return commandServiceImpl.showUserByName(URLDecoder.decode(displayName, "UTF-8"), channelId, callback);
            }
            return commandServiceImpl.showChannelUsers(channelId, callback);

        } catch (Exception e) {
            logger.error("showChannelUsers error", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("/rest/show/user/onlines")
    public CommandResultVO showOnlineUsers(@RequestParam String channelId) {
        try {
            return commandServiceImpl.getOnlineUsers(channelId);
        } catch (Exception e) {
            logger.error("showOnlineUsers error", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("/rest/search/user")
    public CommandResultVO searchUser(@RequestParam String channelId, @RequestParam String key, @RequestParam String callback) {
        try {
            String decoded = URLDecoder.decode(key, "UTF-8");
            return commandServiceImpl.searchUsers(channelId, decoded, callback);
        } catch (Exception e) {
            logger.error("searchUser error", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("/rest/query/ping/mask")
    public CommandResultVO getPingMask(@RequestParam String channelId, @RequestParam String usrId, @RequestParam String discordId) {
        try {
            String decoded = URLDecoder.decode(discordId, "UTF-8");
            return commandServiceImpl.getPingMask(channelId, usrId, decoded);
        } catch (Exception e) {
            logger.error("getPingMask(channelId, usrId, discordId)出错", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("/rest/query/subscribe/mask")
    public CommandResultVO getSubscribeMask(@RequestParam String channelId, @RequestParam String usrId) {
        try {
            return commandServiceImpl.getSubscribeMask(channelId, usrId);
        } catch (Exception e) {
            logger.error("getSubscribeMask(channelId,usrId)出错", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("/rest/update/subscribe/mask")
    public CommandResultVO updateSubscribeMask(@RequestParam String channelId, @RequestParam String usrId, @RequestParam String mask) {
        try {
            return commandServiceImpl.updateSubscribeMask(channelId, usrId, mask);
        } catch (Exception e) {
            logger.error("更新SubscribeMask出错", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("/rest/update/ping/mask")
    public CommandResultVO updatePingMask(@RequestParam String channelId, @RequestParam String usrId, @RequestParam String discordId, @RequestParam String mask) {
        try {
            String decoded = URLDecoder.decode(discordId, "UTF-8");
            return commandServiceImpl.updatePingMask(channelId, usrId, decoded, mask);
        } catch (Exception e) {
            logger.error("更新PingMask出错", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("/rest/query/user/id")
    public CommandResultVO getUserIdByName(@RequestParam String displayName) {
        try {
            String decoded = URLDecoder.decode(displayName, "UTF-8");
            return commandServiceImpl.getUserIdByName(decoded);
        } catch (Exception e) {
            logger.error("查询用户ID出错", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("/websocket")
    public CommandResultVO restartWebsocket(@RequestParam String q) {
        try {
            if (q.equals("restart"))
                return commandServiceImpl.reconnectWebsocket();
            else if (q.equals("status"))
                return commandServiceImpl.getWebsocketStatus();
            else
                return new CommandResultVO().setCode(403).setMsg("不支持的参数");
        } catch (Exception e) {
            logger.error("重连Websocket出错", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("/bot")
    public CommandResultVO restartBot(@RequestParam String q) {
        try {
            if (q.equals("restart"))
                return commandServiceImpl.restartBot();
            else if (q.equals("status"))
                return commandServiceImpl.getBotStatus();
            else
                return new CommandResultVO().setCode(403).setMsg("不支持的参数");
        } catch (Exception e) {
            logger.error("重启Bot出错", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("system/restart")
    public CommandResultVO restart() {
        try {
            commandServiceImpl.restartBot();
            commandServiceImpl.reconnectWebsocket();
            return new CommandResultVO().setCode(200).setMsg("操作成功");
        } catch (Exception e) {
            logger.error("重启系统出错", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }
}
