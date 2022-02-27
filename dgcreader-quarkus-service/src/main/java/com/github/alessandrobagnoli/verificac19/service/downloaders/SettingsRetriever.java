package com.github.alessandrobagnoli.verificac19.service.downloaders;

public interface SettingsRetriever {
    Integer getSettingValue(String settingName, String settingType);

    String getSettingValueAsString(String settingName, String settingType);
}
