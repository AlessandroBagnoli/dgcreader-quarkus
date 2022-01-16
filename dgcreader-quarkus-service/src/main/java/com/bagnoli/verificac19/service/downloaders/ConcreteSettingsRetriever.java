package com.bagnoli.verificac19.service.downloaders;

import javax.enterprise.context.ApplicationScoped;

import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteSettingsRetriever implements SettingsRetriever {

    private final SettingsDownloader settingsDownloader;

    @Override
    public Integer getSettingValue(String settingName, String settingType) {
        return settingsDownloader.downloadSettings()
            .stream()
            .filter(setting -> setting.getName().equals(settingName) && setting.getType()
                .equals(settingType))
            .mapToInt(setting -> Integer.parseInt(setting.getValue()))
            .findFirst()
            .orElseThrow();
    }
}
