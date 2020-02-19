package cn.sola97.vrchat.aop.proxy;

import cn.sola97.vrchat.aop.handler.WsHandler;
import cn.sola97.vrchat.service.CookieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.jetty.JettyWebSocketClient;

public class WebSocketConnectionManagerProxy {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConnectionManagerProxy.class);
    private String webSocketUri;
    private CookieService cookieServiceImpl;
    private JettyWebSocketClient jettyWebSocketClient;
    private WsHandler wsHandler;
    private MyWebSocketConnectionManager myWebSocketConnectionManager;

    public WebSocketConnectionManagerProxy(CookieService cookieServiceImpl, JettyWebSocketClient jettyWebSocketClient, WsHandler wsHandler, String webSocketUri) {
        this.cookieServiceImpl = cookieServiceImpl;
        this.jettyWebSocketClient = jettyWebSocketClient;
        this.wsHandler = wsHandler;
        this.webSocketUri = webSocketUri;
        init();
    }

    public void init() {
        logger.info("Init WebSocketConnectionManager");
        String URL = this.webSocketUri + cookieServiceImpl.getAuthToken();
        this.myWebSocketConnectionManager = new MyWebSocketConnectionManager(jettyWebSocketClient, wsHandler, URL);
        this.myWebSocketConnectionManager.setAutoStartup(true);
        this.myWebSocketConnectionManager.startInternal();
    }

    public void stop() {
        this.myWebSocketConnectionManager.setAutoStartup(false);
        this.myWebSocketConnectionManager.stop();
        this.myWebSocketConnectionManager = null;
    }

    public void rebuild() {
        stop();
        init();
    }
    public Boolean isConnected() {
        if (this.myWebSocketConnectionManager != null) {
            return myWebSocketConnectionManager.isConnected();
        }
        return false;
    }

    public class MyWebSocketConnectionManager extends org.springframework.web.socket.client.WebSocketConnectionManager {

        public MyWebSocketConnectionManager(WebSocketClient client, WebSocketHandler webSocketHandler, String uriTemplate, Object... uriVariables) {
            super(client, webSocketHandler, uriTemplate, uriVariables);
        }

        public boolean isConnected() {
            return super.isConnected();
        }
    }
}
