package cn.sola97.vrchat.aop.handler;


import cn.sola97.vrchat.controller.EventHandlerMapping;
import cn.sola97.vrchat.enums.EventTypeEnums;
import cn.sola97.vrchat.pojo.VRCEventDTO;
import cn.sola97.vrchat.service.CookieService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.jetty.JettyWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;


@Component
public class WsHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(WsHandler.class);
    @Value("${vrchat.websocket-uri}")
    String webSocketUri;
    @Autowired
    CookieService cookieServiceImpl;
    @Autowired
    ConfigurableApplicationContext context;
    @Autowired
    JettyWebSocketClient jettyWebSocketClient;
    @Autowired
    WsHandler wsHandler;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("-------------------VRChat WebSocket Connected-------------------");
    }

    @Autowired
    protected EventHandlerMapping eventHandlerMapping;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(message.getPayload());
            if (json.hasNonNull("err")) {
                logger.warn("err:" + json.get("err").asText() + " payload:" + message.getPayload());
                cookieServiceImpl.deleteCookie();
                session.close(CloseStatus.BAD_DATA);
            }
            String type = json.get("type").asText();
            logger.debug("type:" + type + " palyload:" + message.getPayload());
            if (!EventTypeEnums.getmMap().containsKey(type)) {
                logger.warn("不支持的type:{} palyload:{}", type, message.getPayload());
            }
            eventHandlerMapping.handle(type, mapper.readValue(message.getPayload(), VRCEventDTO.class));
        } catch (Exception e) {
            logger.error("error on handleTextMessage\n payload: " + message.getPayload(), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.warn("Connection closed.");
    }
}