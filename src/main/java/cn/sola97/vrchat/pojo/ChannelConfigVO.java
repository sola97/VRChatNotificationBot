package cn.sola97.vrchat.pojo;

import java.util.List;

public class ChannelConfigVO {

    List<SubscribeDTO> subscribes;

    public List<SubscribeDTO> getSubscribes() {
        return subscribes;
    }

    public void setSubscribes(List<SubscribeDTO> subscribes) {
        this.subscribes = subscribes;
    }
}

