package cn.sola97.vrchat.pojo;

import cn.sola97.vrchat.entity.Ping;
import cn.sola97.vrchat.entity.Subscribe;

import java.util.List;

public class SubscribeDTO extends Subscribe {

    List<Ping> pings;

    public List<Ping> getPings() {
        return pings;
    }

    public void setPings(List<Ping> pings) {
        this.pings = pings;
    }

    @Override
    public String toString() {
        return "SubscribeDTO{" +
                "pings=" + pings +
                "} " + super.toString();
    }
}
