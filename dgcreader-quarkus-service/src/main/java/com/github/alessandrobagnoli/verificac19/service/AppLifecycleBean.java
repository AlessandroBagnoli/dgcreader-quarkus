package com.github.alessandrobagnoli.verificac19.service;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.transaction.Transactional;

import com.github.alessandrobagnoli.verificac19.service.downloaders.CertificatesDownloader;
import com.github.alessandrobagnoli.verificac19.service.downloaders.DrlSynchronizer;
import com.github.alessandrobagnoli.verificac19.service.downloaders.SettingsDownloader;

import io.quarkus.runtime.StartupEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AppLifecycleBean {

    private final CertificatesDownloader certificatesDownloader;
    private final SettingsDownloader settingsDownloader;
    private final DrlSynchronizer drlSynchronizer;

    void onStart(@Observes StartupEvent ev) {
        log.info("Downloading certificates...");
        certificatesDownloader.download();
        log.info("Done downloading certificates.");
        log.info("Downloading settings and blacklisted...");
        settingsDownloader.downloadSettings();
        log.info("Done downloading settings and blacklisted.");
        log.info("Downloading drl...");
        drlSynchronizer.synchronize();
        log.info("Done downloading drl");
        log.info("Application up and running!");
    }
}
