package cn.sola97.vrchat.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum WorldInstanceEnums {
    PUBLIC("Public"),
    HIDDEN("Friends+"),
    FRIENDS("Friends Only"),
    CANR_EQUEST_INVITE("Invite+"),
    PRIVATE("Invite Only");
    private static final Map<String, WorldInstanceEnums> mMap = Collections.unmodifiableMap(Mapping());
    //vrchat://launch?id=wrld_6854fca5-741b-4ac4-b6e4-3b5d67460456:23716~canRequestInvite(usr_30ae0fcd-ed69-4f55-b4f3-34f5272f7344)
    //vrchat://launch?id=wrld_6854fca5-741b-4ac4-b6e4-3b5d67460456:23716~friends(usr_5c59019b-5663-462b-bf38-392dfa3638f6)
    //vrchat://launch?id=wrld_6854fca5-741b-4ac4-b6e4-3b5d67460456:23716~private(usr_5c59019b-5663-462b-bf38-392dfa3638f6)
    //https://vrchat.net/launch?worldId=wrld_4432ea9b-729c-46e3-8eaf-846aa0a37fdd&instanceId=7770~private(usr_5c59019b-5663-462b-bf38-392dfa3638f6)~nonce(3A60C3F537AE45625A082A0D0C568CC43EF74C96DCCEE9A5E28F9AD4E62B0D9F)
    private static Pattern pattern = Pattern.compile("(?:launch\\?worldId=)?(([^=]+):((\\d+|null)(?:~(hidden|friends|canRequestInvite|private)\\((.+)\\)~nonce\\((.+)\\))?))");
    private String text;

    WorldInstanceEnums(String text) {
        this.text = text;
    }
    public static Map<String, String> parseLocation(String location){
        HashMap<String, String> map = new HashMap<>();
        if (location == null) return map;
        location = location.replace("&instanceId=", ":");
        Matcher m = pattern.matcher(location);
        if(m.find()){
            map.put("location", m.group(1));
            map.put("worldId", m.group(2));
            map.put("instance", m.group(3));
            map.put("instanceId", m.group(4));
            map.put("status", mMap.get(m.group(5)).getText());
            map.put("usrId", m.group(6));
            map.put("nonce", m.group(7));
        } else if ("offline".equals(location)) {
            map.put("worldId", "offline");
        } else if ("private".equals(location)) {
            map.put("worldId", "private");
        }
        map.put("username", "");
        return map;
    }

    public static boolean match(String location) {
        if (StringUtils.isEmpty(location)) return false;
        location = location.replace("&instanceId=", ":");
        Matcher m = pattern.matcher(location);
        return m.find();
    }
    public static String getTextByString(String str){
        return mMap.get(str).getText();
    }

    public String getText() {
        return text;
    }

    private static Map<String, WorldInstanceEnums> Mapping() {
        Map<String, WorldInstanceEnums> map = new HashMap<>();
        map.put(null,PUBLIC);
        map.put("public",PUBLIC);
        map.put("hidden",HIDDEN);
        map.put("friends",FRIENDS);
        map.put("canRequestInvite",CANR_EQUEST_INVITE);
        map.put("private",PRIVATE);
        return map;
    }
}