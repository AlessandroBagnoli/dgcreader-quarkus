package com.github.alessandrobagnoli.verificac19.service.downloaders;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;

import com.github.alessandrobagnoli.verificac19.dao.BlackListedPassDAO;
import com.github.alessandrobagnoli.verificac19.dto.Setting;
import com.github.alessandrobagnoli.verificac19.model.BlackListedPass;
import com.github.alessandrobagnoli.verificac19.service.DGCApiService;

import io.quarkus.cache.CacheResult;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteSettingsDownloader implements SettingsDownloader {

    private final DGCApiService dgcApiService;
    private final BlackListedPassDAO blackListedPassDAO;

    @Override
    public Set<Setting> downloadSettings() {
        return new HashSet<>(getSettings());
    }

    @CacheResult(cacheName = "settings-cache")
    Collection<Setting> getSettings() {
        Set<Setting> settings = dgcApiService.getSettings();
        settings.stream()
            .filter(setting -> setting.getName().equals("black_list_uvci"))
            .findFirst()
            .ifPresent(saveBlackListed());
        return settings;
    }

    private Consumer<Setting> saveBlackListed() {
        return setting -> Arrays.stream(setting.getValue().split(";"))
            .forEach(blackListed -> blackListedPassDAO.persist(
                BlackListedPass.builder().kid(blackListed).build()));
    }

}
