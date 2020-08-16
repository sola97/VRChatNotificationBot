package cn.sola97.vrchat.config;

import cn.sola97.vrchat.aop.handler.CmdErrorHandler;
import cn.sola97.vrchat.aop.interceptor.CmdInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;

@Configuration
public class CmdRestTemplateConfig {
    @Value("${web.rootUri}")
    String rootUri;
    @Bean
    public RestTemplate cmdRestTemplate(RestTemplateBuilder builder, CmdErrorHandler cmdErrorHandler, CmdInterceptor cmdInterceptor) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        RestTemplate build = builder.rootUri(rootUri)
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                .additionalMessageConverters(converter)
                .setReadTimeout(Duration.ofSeconds(15))
                .setConnectTimeout(Duration.ofSeconds(15))
                .interceptors(cmdInterceptor)
                .errorHandler(cmdErrorHandler)
                .build();
        build.setRequestFactory(requestFactory);
        return build;
    }
}

