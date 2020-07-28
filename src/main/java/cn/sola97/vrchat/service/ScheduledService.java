package cn.sola97.vrchat.service;

public interface ScheduledService {
    Integer checkOnlineUser(String usrId);

    void checkChannelValid();
}
