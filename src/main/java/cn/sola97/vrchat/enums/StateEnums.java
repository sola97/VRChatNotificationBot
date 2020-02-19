package cn.sola97.vrchat.enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum StateEnums {
    ACTIVE("https://i.loli.net/2019/11/25/NAv5TqwBDofLXji.png"),
    ONLINE("https://i.loli.net/2019/11/25/B7wGrYSUCotHujA.png"),
    OFFLINE("https://i.loli.net/2019/11/25/eXs35ruB2fOV7wL.png");
    private static final Map<String, StateEnums> mMap = Collections.unmodifiableMap(Mapping());
    String url;
    StateEnums(String url) {
        this.url = url;
    }
    static public String getUrlFromString(String status){
        return mMap.get(status).getUrl();
    }

    public String getUrl() {
        return url;
    }

    private static Map<String, StateEnums> Mapping() {
        Map<String, StateEnums> map = new HashMap<String, StateEnums>();
        map.put("active", StateEnums.ACTIVE);
        map.put("online", StateEnums.ONLINE);
        map.put("offline", StateEnums.OFFLINE);
        return map;
    }
}
