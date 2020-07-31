package cn.sola97.vrchat.aop.handler;

import cn.sola97.vrchat.service.CookieService;
import cn.sola97.vrchat.utils.ReflectionUtil;
import org.apache.tomcat.util.buf.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import sun.net.www.MessageHeader;
import sun.net.www.protocol.https.DelegateHttpsURLConnection;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ApiErrorHandler implements ResponseErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiErrorHandler.class);
    @Autowired
    private CookieService cookieServiceImpl;
    @Override
    public boolean hasError(@NotNull ClientHttpResponse clientHttpResponse) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug(getMessage(clientHttpResponse));
        }
        if(clientHttpResponse.getStatusCode()== HttpStatus.UNAUTHORIZED){
            logger.warn(getMessage(clientHttpResponse));
            logger.warn("delete cookie");
            cookieServiceImpl.deleteCookie();
            return true;
        }else if(clientHttpResponse.getStatusCode()==HttpStatus.FORBIDDEN){
            logger.error(getMessage(clientHttpResponse));
        } else if (clientHttpResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
            logger.error(getMessage(clientHttpResponse) + "Body:" + clientHttpResponse.getBody().read());
        }
        return !clientHttpResponse.getStatusCode().is2xxSuccessful();
    }

    @Override
    public void handleError(@NotNull ClientHttpResponse clientHttpResponse) throws IOException {

    }

    @Override
    public void handleError(URI url, HttpMethod method, @NotNull ClientHttpResponse response) throws IOException {
        logger.error(getRequestHeaders(response));
        handleError(response);
    }

    private String getRequestHeaders(ClientHttpResponse clientHttpResponse) {
        HttpURLConnection httpURLConnection = ReflectionUtil.getHttpURLConnection(clientHttpResponse);
        DelegateHttpsURLConnection delegateHttpsURLConnection = ReflectionUtil.getDelegateHttpsURLConnection(httpURLConnection);
        MessageHeader messageHeader = ReflectionUtil.getMessageHeader(delegateHttpsURLConnection);
        assert messageHeader != null;
        Map<String, List<String>> headers = messageHeader.getHeaders();
        List<String> strings = new ArrayList<>();
        headers.keySet()
                .stream()
                .filter(k -> k.startsWith("GET") || k.startsWith("POST"))
                .findFirst()
                .ifPresent(strings::add);
        strings.add("User-Agent:" + headers.get("User-Agent").get(0));
        strings.add("Cookie:" + StringUtils.join(headers.get("Cookie"), ';'));
        strings.add("Host:" + headers.get("Host").get(0));
        strings.add("Accept:" + headers.get("Accept").get(0));
        strings.add("Connection:" + headers.get("Connection").get(0));
        return "\n" + StringUtils.join(strings, '\n');
    }

    private String getMessage(ClientHttpResponse clientHttpResponse) {
        HttpURLConnection httpURLConnection = ReflectionUtil.getHttpURLConnection(clientHttpResponse);
        try {
            return MessageFormat.format("VRChatAPI - {0} {1} {2}",
                    httpURLConnection.getRequestMethod(),
                    httpURLConnection.getURL(),
                    httpURLConnection.getHeaderField(0)).toString();
        } catch (Exception e) {
            logger.error("getMessage error", e);
        }
        return "VRChatAPI - getMessage error";
    }
}
