package cn.sola97.vrchat.enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum EventTypeEnums {
    NONE((byte) 0b0, "无"),
    ACTIVE((byte) 0b1, "好友活跃"),
    ONLINE((byte) 0b10, "好友上线"),
    OFFLINE((byte) 0b100, "好友下线"),
    LOCATION((byte) 0b1000, "好友换图"),
    UPDATE((byte) 0b10000, "好友更新状态"),
    NOTIFICATION((byte) 0b100000, "通知");
    private Byte mask;
    private String description;

    EventTypeEnums(Byte mask, String description) {
        this.mask = mask;
        this.description = description;
    }

    public Byte getMask() {
        return mask;
    }

    public static EventTypeEnums getTypeFromString(String type){
        return mMap.get(type);
    }

    public String getDescription() {
        return description;
    }

    private static final Map<String, EventTypeEnums> mMap = Collections.unmodifiableMap(Mapping());

    private static Map<String, EventTypeEnums> Mapping() {
        Map<String, EventTypeEnums> map = new HashMap<>();
        map.put("friend-active", ACTIVE);
        map.put("friend-location",LOCATION);
        map.put("friend-offline",OFFLINE);
        map.put("friend-update",UPDATE);
        map.put("friend-online",ONLINE);
        map.put("user-active", ACTIVE);
        map.put("user-location", LOCATION);
        map.put("user-offline", OFFLINE);
        map.put("user-update", UPDATE);
        map.put("user-online", ONLINE);
        map.put("notification", NOTIFICATION);
        map.put("none",NONE);
        map.put(null, null);
        return map;
    }

    public static Map<String, EventTypeEnums> getmMap() {
        return mMap;
    }
}