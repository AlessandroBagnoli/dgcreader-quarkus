package com.bagnoli.verificac19.service.downloaders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.bagnoli.verificac19.dto.Setting;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.cache.CacheResult;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteSettingsDownloader implements SettingsDownloader {

    @ConfigProperty(name = "settings-api.url")
    String baseUrlSettings;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Override
    public Set<Setting> downloadSettings() {
        Set<Setting> settings;
        try {
            settings = new HashSet<>(getSettings());
        } catch (IOException e) {
            throw new RuntimeException();
        }
        return settings;
    }

    @CacheResult(cacheName = "settings-cache")
    Collection<Setting> getSettings() throws IOException {
        List<Setting> settings = new ArrayList<>();
        HttpGet httpGet = new HttpGet(baseUrlSettings);
        HttpResponse response = httpClient.execute(httpGet);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK) {
            settings.addAll(objectMapper.readValue(EntityUtils.toString(response.getEntity()),
                new TypeReference<List<Setting>>() {
                }));
        }
        return settings;
    }

}
