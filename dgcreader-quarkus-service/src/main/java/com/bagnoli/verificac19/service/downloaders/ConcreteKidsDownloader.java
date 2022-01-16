package com.bagnoli.verificac19.service.downloaders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.cache.CacheResult;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteKidsDownloader implements KidsDownloader {

    @ConfigProperty(name = "certificates-status-api.url")
    String certificatesStatusUrl;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<String> download() {
        List<String> kids;
        try {
            kids = new ArrayList<>(getKids());
        } catch (IOException e) {
            throw new RuntimeException();
        }
        return kids;
    }

    @CacheResult(cacheName = "kids-cache")
    List<String> getKids() throws IOException {
        List<String> kids = new ArrayList<>();
        HttpGet httpGet = new HttpGet(certificatesStatusUrl);
        HttpResponse response = httpClient.execute(httpGet);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK) {
            String responseString = EntityUtils.toString(response.getEntity());
            kids = Arrays.asList(objectMapper.readValue(responseString, String[].class));
        }

        return kids;
    }
}
