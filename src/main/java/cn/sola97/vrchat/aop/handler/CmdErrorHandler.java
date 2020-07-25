package cn.sola97.vrchat.aop.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.net.URI;

@Component
public class CmdErrorHandler implements ResponseErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(CmdErrorHandler.class);

    @Override
    public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
        if (!clientHttpResponse.getStatusCode().is2xxSuccessful()) {
            logger.error("Command - Response StatusCode:" + clientHttpResponse.getStatusCode());
        } else {
            logger.debug("Command - Response StatusCode:" + clientHttpResponse.getStatusCode());
        }
        return !clientHttpResponse.getStatusCode().is2xxSuccessful();
    }

    @Override
    public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
        logger.error(clientHttpResponse.getRawStatusCode() + clientHttpResponse.getRawStatusCode() + "");
        logger.error(clientHttpResponse.getHeaders().toString());
        logger.error(clientHttpResponse.getBody().toString());
    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
        logger.error("Reponse error:" + method.name() + "  " + url);
        handleError(response);
    }
}
