package cn.sola97.vrchat.service;

import cn.sola97.vrchat.entity.Subscribe;

import java.util.List;

public interface SubscribeService {
    int insSubscribe(Subscribe subscribe);

    int updSubscribe(Subscribe subscribe);

    Boolean existsSubscribe(Subscribe subscribe);

    int insSubscribeElseUpd(Subscribe subscribe);

    int insSubscribeIfNotExists(Subscribe subscribe);

    int delSubscribe(Subscribe subscribe);

    List<Subscribe> selSubscribesByUsrId(String usrId);

    Subscribe selSubscribesByPrimaryKey(Subscribe subscribe);

    Boolean existsSubscribe(String channelId, String usr_id);

    List<Subscribe> selSubscribesByChannelId(String channelId);

    List<Subscribe> selSubscribesByUsrIdNotInChannels(String usrId, List<String> channelIds);

    List<Subscribe> selAllSubscribesNotInUsrIdList(List<String> usrIds);

    boolean disableSubscribeByUsrId(String usrId);
}
