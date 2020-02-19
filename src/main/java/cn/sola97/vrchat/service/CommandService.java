package cn.sola97.vrchat.service;

import cn.sola97.vrchat.pojo.CommandResultVO;
import cn.sola97.vrchat.pojo.MessageDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CommandService {
    @Transactional(rollbackFor = Exception.class)
    CommandResultVO subscribe(String channelId, String channelName, String displayName, List<String> discordIds, List<String> discordNames, Byte subMask, Byte pingMask) throws Exception;

    CommandResultVO showConfigByChannelId(String channelId);

    CommandResultVO showChannelUsers(String channelId, String callback);

    CommandResultVO showUserByName(String displayName, String channelId, String callback);

    CommandResultVO getPingMask(String channelId, String usrId, String discordId);

    CommandResultVO getUserIdByName(String displayName);

    CommandResultVO updatePingMask(String channelId, String usrId, String discordId, String mask);

    CommandResultVO getSubscribeMask(String channelId, String usrId);

    CommandResultVO updateSubscribeMask(String channelId, String usrId, String mask);

    MessageDTO showUser(String usrId, String channelId, String callback);

    CommandResultVO deleteSubscribe(String channelId, String usrId);

    CommandResultVO deletePing(String channelId, String usrId, String discordId);

    CommandResultVO getOnlineUsers(String channelId);

    CommandResultVO reconnectWebsocket();

    CommandResultVO restartBot();

    CommandResultVO getWebsocketStatus();

    CommandResultVO getBotStatus();
}
