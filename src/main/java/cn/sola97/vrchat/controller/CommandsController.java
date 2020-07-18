package cn.sola97.vrchat.controller;

import cn.sola97.vrchat.pojo.CommandResultVO;
import cn.sola97.vrchat.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
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

    @RequestMapping("/rest/add/subscribe/{channelId}/{displayName}")
    public CommandResultVO addSubscribe(@PathVariable String channelId,
                                        @PathVariable String displayName,
                                        String channelName,
                                        @RequestParam(defaultValue = "") List<String> discordIds,
                                        @RequestParam(defaultValue = "") List<String> discordNames,
                                        @RequestParam(name = "pingmask", required = false) String pingMask,
                                        @RequestParam(name = "submask", required = false) String subMask) {
        try {
            Byte byteSubMask = Byte.valueOf(defaultSubMask);
            Byte bytePingMask = Byte.valueOf(defaultPingMask);
            if (pingMask != null && !"".equals(pingMask)) bytePingMask = Byte.valueOf(pingMask);
            if (subMask != null && !"".equals(subMask)) byteSubMask = Byte.valueOf(subMask);

            String decodedName = URLDecoder.decode(displayName, "utf8");
            List<String> decodedDiscordIds = new ArrayList<>();
            for (String discordId : discordIds) {
                decodedDiscordIds.add(URLDecoder.decode(discordId, "utf8"));
            }
            return commandServiceImpl.subscribe(channelId, channelName, decodedName, decodedDiscordIds, discordNames, byteSubMask, bytePingMask);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("/rest/delete/subscribe/{channelId}/{usrId}")
    public CommandResultVO deleteSubscribe(@PathVariable String channelId, @PathVariable String usrId) {
        try {
            return commandServiceImpl.deleteSubscribe(channelId, usrId);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("/rest/delete/ping/{channelId}/{usrId}")
    public CommandResultVO deleteSubscribe(@PathVariable String channelId, @PathVariable String usrId, @RequestParam String discordId) {
        try {
            String decoded = URLDecoder.decode(discordId, "UTF-8");
            return commandServiceImpl.deletePing(channelId, usrId, decoded);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }


    @RequestMapping("/rest/show/config/{channelId}")
    public CommandResultVO showChannelConfig(@PathVariable String channelId) {
        try {
            return commandServiceImpl.showConfigByChannelId(channelId);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("/rest/show/user/{channelId}")
    public CommandResultVO showChannelUsers(@PathVariable String channelId, @RequestParam String callback) {
        try {
            return commandServiceImpl.showChannelUsers(channelId, callback);
        } catch (Exception e) {
            logger.error("show user failed", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("/rest/show/user/onlines")
    public CommandResultVO showOnlineUsers(@RequestParam String channelId) {
        try {
            return commandServiceImpl.getOnlineUsers(channelId);
        } catch (Exception e) {
            logger.error("show user failed", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("/rest/show/user/{channelId}/{displayName}")
    public CommandResultVO showUser(@PathVariable String channelId, @PathVariable String displayName, @RequestParam String callback) {
        try {
            return commandServiceImpl.showUserByName(displayName, channelId, callback);
        } catch (Exception e) {
            logger.error("show user failed", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("/rest/show/userid/{channelId}/{userId}")
    public CommandResultVO showUserById(@PathVariable String channelId, @PathVariable String userId, @RequestParam String callback) {
        try {
            return commandServiceImpl.showUserById(userId, channelId, callback);
        } catch (Exception e) {
            logger.error("show user failed", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("/rest/search/user/{channelId}/{search}")
    public CommandResultVO searchUser(@PathVariable String channelId, @PathVariable String search, @RequestParam String callback) {
        try {
            String decoded = URLDecoder.decode(search, "UTF-8");
            return commandServiceImpl.searchUsers(channelId, decoded, callback);
        } catch (Exception e) {
            logger.error("search user failed", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("/rest/query/ping/mask/{channelId}/{usrId}")
    public CommandResultVO getPingMask(@PathVariable String channelId, @PathVariable String usrId, @RequestParam String discordId) {
        try {
            String decoded = URLDecoder.decode(discordId, "UTF-8");
            return commandServiceImpl.getPingMask(channelId, usrId, decoded);
        } catch (Exception e) {
            logger.error("获取mask失败", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("/rest/query/subscribe/mask/{channelId}/{usrId}")
    public CommandResultVO getPingMask(@PathVariable String channelId, @PathVariable String usrId) {
        try {
            return commandServiceImpl.getSubscribeMask(channelId, usrId);
        } catch (Exception e) {
            logger.error("获取subscribe mask失败", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("/rest/update/subscribe/mask/{channelId}/{usrId}")
    public CommandResultVO updateSubscribeMask(@PathVariable String channelId, @PathVariable String usrId, @RequestParam String mask) {
        try {
            return commandServiceImpl.updateSubscribeMask(channelId, usrId, mask);
        } catch (Exception e) {
            logger.error("更新subscribe mask失败", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("/rest/update/ping/mask/{channelId}/{usrId}")
    public CommandResultVO updatePingMask(@PathVariable String channelId, @PathVariable String usrId, @RequestParam String discordId, @RequestParam String mask) {
        try {
            String decoded = URLDecoder.decode(discordId, "UTF-8");
            return commandServiceImpl.updatePingMask(channelId, usrId, decoded, mask);
        } catch (Exception e) {
            logger.error("更新ping mask失败", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }

    @RequestMapping("/rest/query/user/{displayName}")
    public CommandResultVO getUserIdByName(@PathVariable String displayName) {
        try {
            return commandServiceImpl.getUserIdByName(displayName);
        } catch (Exception e) {
            logger.error("查询用户ID失败", e);
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
            logger.error("重连Websocket失败", e);
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
            logger.error("重启Bot失败", e);
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
            logger.error("重启系统失败", e);
            return new CommandResultVO().setCode(500).setMsg(e.getMessage());
        }
    }
}
