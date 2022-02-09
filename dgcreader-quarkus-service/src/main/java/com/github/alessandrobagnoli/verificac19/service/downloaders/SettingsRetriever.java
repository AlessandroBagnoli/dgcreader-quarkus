package com.github.alessandrobagnoli.verificac19.service.downloaders;

@FunctionalInterface
public interface SettingsRetriever {
    Integer getSettingValue(String settingName, String settingType);
}
