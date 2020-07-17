package cn.sola97.vrchat.aop.interceptor;

import cn.sola97.vrchat.service.CookieService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

@Component
public class ApiInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(ApiInterceptor.class);
    @Autowired
    CookieService cookieServiceImpl;

    @NotNull
    @Override
    public ClientHttpResponse intercept(HttpRequest request, @NotNull byte[] body, ClientHttpRequestExecution execution) throws IOException {
        long start = System.currentTimeMillis();
        URI uri = UriComponentsBuilder.fromUri(request.getURI()).queryParam("apiKey", "JlE5Jldo5Jibnk5O5hTx6XVqsJu4WJ26").build().toUri();
        HttpHeaders headers = request.getHeaders();
        headers.add(HttpHeaders.COOKIE,cookieServiceImpl.getCookie());
        logger.info("VRChatAPI - " + request.getMethod() + " " + request.getURI());
        return execution.execute(new MyHttpRequestWrapper(request, uri, headers), body);
    }

    class MyHttpRequestWrapper extends HttpRequestWrapper {
        private URI uri;
        private HttpHeaders headers;

        MyHttpRequestWrapper(HttpRequest request, URI uri, HttpHeaders headers) {
            super(request);
            this.uri=uri;
            this.headers = headers;
        }

        @NotNull
        @Override
        public URI getURI() {
            return uri;
        }

        @NotNull
        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }
    }
}
