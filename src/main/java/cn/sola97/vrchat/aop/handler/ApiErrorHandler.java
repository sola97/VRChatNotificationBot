package cn.sola97.vrchat.aop.handler;

import cn.sola97.vrchat.service.CookieService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.net.URI;

@Component
public class ApiErrorHandler implements ResponseErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiErrorHandler.class);
    @Autowired
    private CookieService cookieServiceImpl;
    @Override
    public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
        logger.debug("Status code: " + clientHttpResponse.getStatusCode());
        if(clientHttpResponse.getStatusCode()== HttpStatus.UNAUTHORIZED){
            cookieServiceImpl.deleteCookie();
            return true;
        }else if(clientHttpResponse.getStatusCode()==HttpStatus.FORBIDDEN){
            logger.warn("Call returned a error 403 forbidden resposne");
        }
        return !clientHttpResponse.getStatusCode().is2xxSuccessful();
    }

    @Override
    public void handleError(@NotNull ClientHttpResponse clientHttpResponse) throws IOException {

    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
        logger.error("HTTP {} url:{} method:{}", response.getStatusCode(), url, method);
        handleError(response);
    }

}
