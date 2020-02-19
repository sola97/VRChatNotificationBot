package cn.sola97.vrchat.enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ReleaseStatusEnums {
    PUBLIC("Public"),
    HIDDEN("Friends+"),
    FRIENDS("Friends Only"),
    CANR_EQUEST_INVITE("Invite+"),
    PRIVATE("Invite Only");
    //vrchat://launch?id=wrld_6854fca5-741b-4ac4-b6e4-3b5d67460456:23716~canRequestInvite(usr_30ae0fcd-ed69-4f55-b4f3-34f5272f7344)
    //vrchat://launch?id=wrld_6854fca5-741b-4ac4-b6e4-3b5d67460456:23716~friends(usr_5c59019b-5663-462b-bf38-392dfa3638f6)
    //vrchat://launch?id=wrld_6854fca5-741b-4ac4-b6e4-3b5d67460456:23716~private(usr_5c59019b-5663-462b-bf38-392dfa3638f6)
    private static Pattern pattern = Pattern.compile("(.+):(\\d+|null)(?:~(hidden|friends|canRequestInvite|private)\\((.+)\\)~nonce\\((.+)\\))?");
    private static final Map<String, ReleaseStatusEnums> mMap = Collections.unmodifiableMap(Mapping());
    private String text;
    ReleaseStatusEnums(String text) {
        this.text = text;
    }
    public static Map<String, String> parseLocation(String location){
        HashMap<String, String> map = new HashMap<>();
        if (location == null) return map;
        Matcher m = pattern.matcher(location);
        if(m.find()){
            map.put("worldId",m.group(1));
            map.put("instanceId",m.group(2));
            map.put("status",mMap.get(m.group(3)).getText());
            map.put("usrId",m.group(4));
            map.put("nonce",m.group(5));
        } else if ("offline".equals(location)) {
            map.put("worldId", "offline");
        } else if ("private".equals(location)) {
            map.put("worldId", "private");
        }
        return map;
    }
    public static String getTextByString(String str){
        return mMap.get(str).getText();
    }

    public String getText() {
        return text;
    }

    private static Map<String, ReleaseStatusEnums> Mapping() {
        Map<String, ReleaseStatusEnums> map = new HashMap<>();
        map.put(null,PUBLIC);
        map.put("public",PUBLIC);
        map.put("hidden",HIDDEN);
        map.put("friends",FRIENDS);
        map.put("canRequestInvite",CANR_EQUEST_INVITE);
        map.put("private",PRIVATE);
        return map;
    }
}