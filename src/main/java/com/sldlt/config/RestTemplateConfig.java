package com.sldlt.config;

import java.util.Locale.LanguageRange;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        final RestTemplate restTemplate = restTemplateBuilder.build();

        // Connect timeout
        final ConnectionConfig connectionConfig = ConnectionConfig.custom().setConnectTimeout(Timeout.ofMilliseconds(60000)).build();

        // Socket timeout
        final SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(Timeout.ofMilliseconds(60000)).build();

        // Connection request timeout
        final RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(Timeout.ofMilliseconds(60000)).build();

        final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultSocketConfig(socketConfig);
        connectionManager.setDefaultConnectionConfig(connectionConfig);

        final HttpClient httpClient = HttpClientBuilder.create().setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig).build();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));

        return restTemplate;
    }

    @Bean
    public HttpHeaders httpHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate");
        headers.setAcceptLanguage(LanguageRange.parse("en"));
        headers.add(HttpHeaders.USER_AGENT, "I am a real person I swear ;)");
        return headers;
    }
}
