package cn.sola97.vrchat.service;

import cn.sola97.vrchat.pojo.CommandResultVO;
import cn.sola97.vrchat.pojo.MessageDTO;
import org.jetbrains.annotations.NotNull;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CommandService {

    @Transactional(rollbackFor = Exception.class)
    CommandResultVO subscribe(@NotNull String userKey, @NotNull String channelId, @NotNull String channelName, @NotNull List<String> discordIds, @NotNull List<String> discordNames, @NotNull Byte subMask, @NotNull Byte pingMask) throws Exception;

    CommandResultVO showConfigByChannelId(String channelId);

    CommandResultVO showUserByChannelId(String channelId, String callback);

    CommandResultVO showUserByName(String displayName, String channelId, String callback);

    CommandResultVO getPingMask(String channelId, String usrId, String discordId);

    CommandResultVO getUserIdByName(String displayName);

    CommandResultVO updatePingMask(String channelId, String usrId, String discordId, String mask);

    CommandResultVO getSubscribeMask(String channelId, String usrId);

    CommandResultVO updateSubscribeMask(String channelId, String usrId, String mask);

    MessageDTO showUser(String usrId, String channelId, String callback);

    MessageDTO joinUser(String usrId, String channelId, String callback);

    CommandResultVO deleteSubscribe(String channelId, String usrId);

    CommandResultVO deletePing(String channelId, String usrId, String discordId);

    CommandResultVO getOnlineUsers(String channelId);

    CommandResultVO searchUsers(String channelId, String searchKey, String callback);

    CommandResultVO reconnectWebsocket();

    CommandResultVO restartBot();

    CommandResultVO getWebsocketStatus();

    CommandResultVO getBotStatus();

    CommandResultVO showUserById(String userId, String channelId1, String callback);

    CommandResultVO joinUserById(String userId, String channelId, String callback);

    CommandResultVO joinUserByName(String decode, String channelId, String callback);

    CommandResultVO joinUserByChannelId(String channelId, String callback);

    CommandResultVO joinLaunchURL(String channelId, String worldId, String instanceId, String callback);
}
