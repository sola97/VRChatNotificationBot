package cn.sola97.vrchat.enums;

import java.util.HashSet;
import java.util.List;

public enum TrustCorlorEnums {
    FRIENDS(0xffff00),
    VISITOR(0x808080),
    NEWUSER(0x1778ff),
    USER(0x2bcf5c),
    KNOWNUSER(0xff7b42),
    TRUSTEDUSER(0x8143e6),
    VETERAN(0xffff00);

    private int value;
    TrustCorlorEnums(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static int getByTags(List<String> tags){
        HashSet<String> tagsSet = new HashSet<>(tags);
        if(tagsSet.contains("system_trust_legend")){
            return VETERAN.getValue();
        }
        if(tagsSet.contains("system_trust_veteran")){
            return TRUSTEDUSER.getValue();
        }
        if(tagsSet.contains("system_trust_trusted")){
            return KNOWNUSER.getValue();
        }
        if(tagsSet.contains("system_trust_known")){
            return USER.getValue();
        }
        if(tagsSet.contains("system_trust_basic")){
            return NEWUSER.getValue();
        }
        return VISITOR.getValue();

    }

}
