package com.bagnoli.verificac19.service.downloaders;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.bagnoli.verificac19.dto.Setting;
import com.bagnoli.verificac19.service.restclient.DGCApiService;

import io.quarkus.cache.CacheResult;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteSettingsDownloader implements SettingsDownloader {

    private final DGCApiService dgcApiService;

    @Override
    public Set<Setting> downloadSettings() {
        return new HashSet<>(getSettings());
    }

    @CacheResult(cacheName = "settings-cache")
    Collection<Setting> getSettings() {
        return dgcApiService.getSettings();
    }

}
