package cn.sola97.vrchat.service;

import cn.sola97.vrchat.entity.Channel;

import java.util.List;

public interface ChannelService {
    Channel selChannelById(String channelId);

    Boolean existsChannel(String channelId);

    int insChannel(Channel channel);

    int updChannelByPrimaryKey(Channel channel);

    int insChannelElseUpd(Channel channel);

    List<Channel> selAllChannel();

    List<Channel> selDisabledChannel();

    boolean disableChannelByChannelId(String channelId);

    boolean enableChannelByChannelId(String channelId);
}
