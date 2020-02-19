package cn.sola97.vrchat.config;

import cn.sola97.vrchat.aop.handler.WsHandler;
import cn.sola97.vrchat.aop.proxy.WebSocketConnectionManagerProxy;
import cn.sola97.vrchat.service.CookieService;
import cn.sola97.vrchat.utils.HttpUtil;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpProxy;
import org.eclipse.jetty.client.ProxyConfiguration;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.jetty.JettyWebSocketClient;

@Configuration
public class WsConfig {
    Logger logger = LoggerFactory.getLogger(WsConfig.class);
    @Value("${vrchat.api.proxy:}")
    String proxyString;
    @Autowired
    CookieService cookieServiceImpl;
    @Value("${vrchat.websocket-uri}")
    String webSocketUri;

    @Bean
    public JettyWebSocketClient getJettyWebSocketClient() throws Exception {
        JettyWebSocketClient jettyWebSocketClient;
        String[] hostAndPort = HttpUtil.getHostAndPort(proxyString);
        if (hostAndPort != null) {
            try {
                HttpClient httpClient = new HttpClient(new SslContextFactory.Client());
                ProxyConfiguration proxyConfig = httpClient.getProxyConfiguration();
                HttpProxy proxy = new HttpProxy(hostAndPort[0], Integer.valueOf(hostAndPort[1]));
                proxyConfig.getProxies().add(proxy);
                WebSocketClient webSocketClient = new WebSocketClient(httpClient);
                httpClient.start();
                jettyWebSocketClient = new JettyWebSocketClient(webSocketClient);
            } catch (Exception e) {
                logger.error("设置websocket客户端的代理失败，请检查vrchat.api.proxy=" + proxyString + "是否正确？");
                throw e;
            }
        } else {
            jettyWebSocketClient = new JettyWebSocketClient();
        }
        return jettyWebSocketClient;
    }

    @Bean
    public WebSocketConnectionManagerProxy webSocketConnectionManagerProxy(CookieService cookieServiceImpl, JettyWebSocketClient jettyWebSocketClient, WsHandler wsHandler) {
        return new WebSocketConnectionManagerProxy(cookieServiceImpl, jettyWebSocketClient, wsHandler, webSocketUri);
    }
}
