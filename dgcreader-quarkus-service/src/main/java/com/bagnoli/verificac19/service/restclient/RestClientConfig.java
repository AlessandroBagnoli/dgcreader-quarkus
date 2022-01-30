package com.bagnoli.verificac19.service.restclient;

import java.net.URL;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.net.ssl.SSLContext;

import org.apache.http.ssl.SSLContextBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import io.netty.handler.ssl.SslProtocols;
import io.quarkus.arc.DefaultBean;
import lombok.SneakyThrows;

@Dependent
public class RestClientConfig {

    @ConfigProperty(name = "settings-api.baseurl")
    String baseUrl;

    @SneakyThrows
    @Produces
    @DefaultBean
    public DGCApiService dgcApiService() {
        SSLContext sslContext = SSLContextBuilder.create()
            .setProtocol(SslProtocols.TLS_v1_2)
            .build();
        return RestClientBuilder.newBuilder()
            .baseUrl(new URL(baseUrl))
            .sslContext(sslContext)
            .build(DGCApiService.class);
    }

}
