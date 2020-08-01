package cn.sola97.vrchat.aop.handler;

import cn.sola97.vrchat.entity.ResponseError;
import cn.sola97.vrchat.service.CookieService;
import cn.sola97.vrchat.service.PingService;
import cn.sola97.vrchat.service.SubscribeService;
import cn.sola97.vrchat.utils.ReflectionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.buf.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import sun.net.www.MessageHeader;
import sun.net.www.protocol.https.DelegateHttpsURLConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ApiErrorHandler implements ResponseErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiErrorHandler.class);
    @Autowired
    private CookieService cookieServiceImpl;
    @Autowired
    private PingService pingServiceImpl;
    @Autowired
    private SubscribeService subscribeServiceImpl;
    private ThreadLocal<String> body = new ThreadLocal<>();

    @Override
    public boolean hasError(@NotNull ClientHttpResponse clientHttpResponse) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug(getMessage(clientHttpResponse));
        }
        if (clientHttpResponse.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            logger.warn(getMessage(clientHttpResponse));
            logger.warn("正在清除Cookie");
            cookieServiceImpl.deleteCookie();
            return true;
        } else if (clientHttpResponse.getStatusCode() == HttpStatus.FORBIDDEN) {
            logger.error(getMessage(clientHttpResponse));
        } else if (clientHttpResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
            logger.error(getMessage(clientHttpResponse));
        }
        return !clientHttpResponse.getStatusCode().is2xxSuccessful();
    }

    @Override
    public void handleError(@NotNull ClientHttpResponse clientHttpResponse) throws IOException {
        //GET /api/1/users/usr_43134ce0-2357-48fb-9de0-5c7947088cd5?apiKey=JlE5Jldo5Jibnk5O5hTx6XVqsJu4WJ26 HTTP/1.1
        String body = getBodyString(clientHttpResponse);
        logger.error(getRequestHeaders(clientHttpResponse) + "\n" + body);
        ObjectMapper mapper = new ObjectMapper();
        ResponseError responseError = mapper.readValue(body, ResponseError.class);
        String message = responseError.getError().getMessage();
        Pattern pattern = Pattern.compile("User (usr_.+) not found");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            String usrId = matcher.group(1);
            logger.info("好友{}不存在", usrId);
            if (pingServiceImpl.disablePingByUsrId(usrId)) {
                logger.info("disable {} 的Ping提醒成功", usrId);
            }
            if (subscribeServiceImpl.disableSubscribeByUsrId(usrId)) {
                logger.info("disable {} 的Subscribe提醒成功", usrId);
            }
        }
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
                    httpURLConnection.getHeaderField(0));
        } catch (Exception e) {
            logger.error("getMessage error", e);
        }
        return "VRChatAPI - getMessage error";
    }

    private String getBodyString(ClientHttpResponse response) {
        if (!StringUtil.isEmpty(body.get())) return body.get();
        try {
            if (response != null && response.getBody() != null) {
                StringBuilder inputStringBuilder = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8));
                String line = bufferedReader.readLine();
                while (line != null) {
                    inputStringBuilder.append(line);
                    inputStringBuilder.append('\n');
                    line = bufferedReader.readLine();
                }
                String body = inputStringBuilder.toString();
                this.body.set(body);
                return body;
            } else {
                return null;
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
