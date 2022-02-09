package com.github.alessandrobagnoli.verificac19.service.downloaders;

import java.util.Set;

import com.github.alessandrobagnoli.verificac19.dto.Setting;

@FunctionalInterface
public interface SettingsDownloader {
    Set<Setting> downloadSettings();
}
