package cn.sola97.vrchat.enums;

import java.util.HashSet;
import java.util.List;

public enum TrustCorlorEnums {
    FRIENDS(0xffff00, "\uD83D\uDFE8"),
    VISITOR(0x808080, "⬜"),
    NEWUSER(0x1778ff, "\uD83D\uDFE6"),
    USER(0x2bcf5c, "\uD83D\uDFE9"),
    KNOWNUSER(0xff7b42, "\uD83D\uDFE7"),
    TRUSTEDUSER(0x8143e6, "\uD83D\uDFEA"),
    VETERAN(0xffff00, "\uD83D\uDFE8");

    private int value;
    private String emoji;

    TrustCorlorEnums(int value, String emoji) {
        this.value = value;
        this.emoji = emoji;
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

    public static String getEmojiByTags(List<String> tags) {
        HashSet<String> tagsSet = new HashSet<>(tags);
        if (tagsSet.contains("system_trust_legend")) {
            return VETERAN.getEmoji();
        }
        if (tagsSet.contains("system_trust_veteran")) {
            return TRUSTEDUSER.getEmoji();
        }
        if (tagsSet.contains("system_trust_trusted")) {
            return KNOWNUSER.getEmoji();
        }
        if (tagsSet.contains("system_trust_known")) {
            return USER.getEmoji();
        }
        if (tagsSet.contains("system_trust_basic")) {
            return NEWUSER.getEmoji();
        }
        return VISITOR.getEmoji();
    }

    public String getEmoji() {
        return emoji;
    }

}
