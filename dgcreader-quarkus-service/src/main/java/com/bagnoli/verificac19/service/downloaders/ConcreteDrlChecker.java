package com.bagnoli.verificac19.service.downloaders;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.enterprise.context.ApplicationScoped;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.bagnoli.verificac19.dto.CrlStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.cache.CacheResult;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteDrlChecker implements DrlChecker {

    @ConfigProperty(name = "crl-status-check-api.url")
    String crlStatusCheckUrl;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Override
    public CrlStatus check(Long version, Long chunk) {
        CrlStatus crlStatus;
        try {
            crlStatus = downloadStatus(version, chunk);
        } catch (IOException | URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
        return crlStatus;
    }

    @CacheResult(cacheName = "crl-status-cache")
    CrlStatus downloadStatus(Long version, Long chunk) throws IOException, URISyntaxException {
        URIBuilder builder = new URIBuilder(crlStatusCheckUrl);
        builder.setParameter("version", version.toString()).setParameter("chunk", chunk.toString());
        HttpGet httpGet = new HttpGet(builder.build());
        HttpResponse response = httpClient.execute(httpGet);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK) {
            return objectMapper.readValue(EntityUtils.toString(response.getEntity()),
                CrlStatus.class);
        } else {
            throw new RuntimeException("Got " + statusCode + " from the crl status check service");
        }
    }

}
