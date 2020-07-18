package cn.sola97.vrchat.service;

import cn.sola97.vrchat.entity.Ping;

import java.util.List;

public interface PingService {

    List<Ping> selPingByChannelIdAndUserId(String channelId, String usrId);

    int insPing(Ping ping);

    int updPing(Ping ping);

    int updPingByChannelIdAndUsrId(Ping ping);

    int delPingByPrimaryKey(Ping ping);

    int delPingByChannelIdAndUsrId(Ping ping);

    int insPingElseUpd(Ping ping);

    Byte selMaskByChannelIdAndUsrId(Ping ping);

    Ping selPingByPrimaryKey(Ping ping);

    Boolean existsPing(Ping ping);

    List<Ping> selAllPingNotInUsrIdList(List<String> usrIds);
}
