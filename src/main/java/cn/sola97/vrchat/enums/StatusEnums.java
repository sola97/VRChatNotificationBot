package cn.sola97.vrchat.enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum  StatusEnums {
    ACTIVE("https://i.loli.net/2019/11/25/B7wGrYSUCotHujA.png"),
    JOIN_ME("https://i.loli.net/2019/11/25/Ul58EY2vhS4cJmT.png"),
    BUSY("https://i.loli.net/2019/11/25/imcKOH9ubZC2xVt.png");
    private static final Map<String, StatusEnums> mMap = Collections.unmodifiableMap(Mapping());
    String url;
    StatusEnums(String url) {
        this.url = url;
    }
    static public String getUrlFromString(String status){
        return mMap.get(status).getUrl();
    }

    public String getUrl() {
        return url;
    }

    private static Map<String, StatusEnums> Mapping() {
        Map<String, StatusEnums> map = new HashMap<String, StatusEnums>();
        map.put("active",StatusEnums.ACTIVE);
        map.put("join me",StatusEnums.JOIN_ME);
        map.put("busy",StatusEnums.BUSY);
        return map;
    }
}
