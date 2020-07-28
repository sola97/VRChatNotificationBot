package cn.sola97.vrchat.aop.handler;

import cn.sola97.vrchat.utils.ReflectionUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

@Component
public class CmdErrorHandler implements ResponseErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(CmdErrorHandler.class);

    @Override
    public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
        if (logger.isDebugEnabled()) {
            HttpURLConnection httpURLConnection = ReflectionUtil.getHttpURLConnection(clientHttpResponse);
            if (httpURLConnection != null) {
                logger.debug("Command - {} {} {}",
                        httpURLConnection.getRequestMethod(),
                        httpURLConnection.getURL(),
                        httpURLConnection.getHeaderField(0));
            }
        }
        return !clientHttpResponse.getStatusCode().is2xxSuccessful();
    }

    @Override
    public void handleError(@NotNull ClientHttpResponse clientHttpResponse) throws IOException {
    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
        HttpURLConnection httpURLConnection = ReflectionUtil.getHttpURLConnection(response);
        assert httpURLConnection != null;
        logger.error("Command - {} {} {}",
                httpURLConnection.getRequestMethod(),
                httpURLConnection.getURL(),
                httpURLConnection.getHeaderField(0));
        handleError(response);
    }
}
