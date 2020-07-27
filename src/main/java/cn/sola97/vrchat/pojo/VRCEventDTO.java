package cn.sola97.vrchat.pojo;

import cn.sola97.vrchat.enums.EventTypeEnums;
import cn.sola97.vrchat.pojo.impl.WsFriendContent;
import cn.sola97.vrchat.pojo.impl.WsNotificationContent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VRCEventDTO<T> {
    private static final Logger logger = LoggerFactory.getLogger(VRCEventDTO.class);
    private EventTypeEnums type;
    private T content;
    @JsonIgnore
    private List<MessageDTO> messages = new ArrayList<>();
    private TypeReference typeReference;

    @JsonCreator
    public VRCEventDTO(@JsonProperty("type") String type) {
        if (type.startsWith("friend") || type.startsWith("user")) {
            typeReference = new TypeReference<WsFriendContent>() {
            };
        } else if (type.startsWith("notification")) {
            typeReference = new TypeReference<WsNotificationContent>() {
            };
        } else {
            logger.warn("不支持的类型type：{}", type);
        }
        this.setType(type);
    }

    public VRCEventDTO() {

    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public void setContent(String content) throws JsonProcessingException {
        if (content.equals("")) return;
        ObjectMapper objectMapper = new ObjectMapper();
        content = content.replaceAll(",\"details\":\"\\{}\"", "");
        if (typeReference == null) {
            logger.warn("不支持的类型content：{}", content);
        }
        this.content = (T) objectMapper.readValue(content, typeReference);
    }

    public Byte getTypeMask() {
        return type.getMask();
    }

    public EventTypeEnums getType() {
        return type;
    }

    public void setType(EventTypeEnums type) {
        this.type = type;
    }

    public void setType(String type) {
        this.type= EventTypeEnums.getTypeFromString(type);
    }

    public List<MessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageDTO> messages) {
        this.messages = messages;
    }

    public boolean isCreatedByFactory() {
        return typeReference == null;
    }

    @Override
    public String toString() {
        return "VRCEventDTO{" +
                "type=" + type +
                ", content=" + content +
                ", messages=" + messages +
                ", typeReference=" + typeReference +
                '}';
    }
}
