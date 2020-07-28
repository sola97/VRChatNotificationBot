package cn.sola97.vrchat.pojo;

import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageDTO {
    private typeEnums type;
    private String content;
    private String channelId;
    private String callback;
    private EmbedBuilder embedBuilder;
    private List<String> pings = new ArrayList<>();
    private Map<String,String> locationMap;
    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public EmbedBuilder getEmbedBuilder() {
        return embedBuilder;
    }

    public void setEmbedBuilder(EmbedBuilder embedBuilder) {
        this.embedBuilder = embedBuilder;
    }

    public List<String> getPings() {
        return pings;
    }

    public void setPings(List<String> pings) {
        this.pings = pings;
    }
    public void addPing(String discordId){
        pings.add(discordId);
    }

    public Map<String, String> getLocationMap() {
        return locationMap;
    }

    public void setLocationMap(Map<String, String> locationMap) {
        this.locationMap = locationMap;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public typeEnums getType() {
        return type;
    }

    public void setType(typeEnums type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "MessageDTO{" +
                "type=" + type +
                ", content='" + content + '\'' +
                ", channelId='" + channelId + '\'' +
                ", callback='" + callback + '\'' +
                ", embedBuilder=" + ReflectionToStringBuilder.toString(embedBuilder, ToStringStyle.SHORT_PREFIX_STYLE) +
                ", pings=" + pings +
                ", locationMap=" + locationMap +
                '}';
    }

    public enum typeEnums {
        SEND_TO_CHANNEL,
        SEND_TO_OWNER,
        EDIT_MESSAGE
    }
}
