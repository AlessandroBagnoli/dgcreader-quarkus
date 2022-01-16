package com.bagnoli.verificac19.service;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import com.bagnoli.verificac19.service.downloaders.CertificatesDownloader;
import com.bagnoli.verificac19.service.downloaders.SettingsDownloader;

import io.quarkus.runtime.StartupEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
public class AppLifecycleBean {

    private final CertificatesDownloader certificatesDownloader;
    private final SettingsDownloader settingsDownloader;

    void onStart(@Observes StartupEvent ev) {
        log.info("Downloading certificates...");
        certificatesDownloader.download();
        log.info("Done downloading certificates.");
        log.info("Downloading settings...");
        settingsDownloader.downloadSettings();
        log.info("Done downloading settings, application up and running!");
    }
}
