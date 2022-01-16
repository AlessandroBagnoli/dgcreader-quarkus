package com.bagnoli.verificac19.service.downloaders;

import java.util.Set;

import com.bagnoli.verificac19.dto.Setting;

@FunctionalInterface
public interface SettingsDownloader {
    Set<Setting> downloadSettings();
}
