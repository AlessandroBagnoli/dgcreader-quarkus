package com.bagnoli.verificac19.config;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.net.ssl.SSLContext;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import io.netty.handler.ssl.SslProtocols;

@ApplicationScoped
public class HttpClientFactory {

    @Produces
    public HttpClient getHttpClient()
        throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        SSLContext sslContext =
            new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build();

        SSLConnectionSocketFactory sslConnectionSocketFactory =
            new SSLConnectionSocketFactory(sslContext,
                new String[] {SslProtocols.TLS_v1_2},
                null,
                NoopHostnameVerifier.INSTANCE);

        return HttpClients
            .custom()
            .setSSLSocketFactory(sslConnectionSocketFactory)
            .build();
    }

}
