package cn.sola97.vrchat.config;

import cn.sola97.vrchat.aop.handler.ApiErrorHandler;
import cn.sola97.vrchat.aop.interceptor.ApiInterceptor;
import cn.sola97.vrchat.utils.HttpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.net.Proxy;
import java.time.Duration;
import java.util.Collections;

@Configuration
public class ApiRestTemplateConfig {
    @Value("${vrchat.rootUri}")
    String rootUri;
    @Value("${vrchat.api.proxy:}")
    String proxyString;
    @Bean
    public RestTemplate apiRestTemplate(RestTemplateBuilder builder, ApiErrorHandler apiErrorHandler, ApiInterceptor apiInterceptor) {
        Proxy proxy = HttpUtil.getProxy(proxyString);
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        if (proxy != null)
            requestFactory.setProxy(proxy);
        RestTemplate restTemplate = builder.rootUri(rootUri)
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                .interceptors(apiInterceptor)
                .setReadTimeout(Duration.ofSeconds(10))
                .setConnectTimeout(Duration.ofSeconds(5))
                .errorHandler(apiErrorHandler)
                .additionalMessageConverters(converter)
                .build();
        restTemplate.setRequestFactory(requestFactory);
        return restTemplate;
    }
}

