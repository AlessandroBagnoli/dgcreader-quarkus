package com.bagnoli.verificac19.service;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.transaction.Transactional;

import com.bagnoli.verificac19.service.downloaders.CertificatesDownloader;
import com.bagnoli.verificac19.service.downloaders.DrlChecker;
import com.bagnoli.verificac19.service.downloaders.DrlDownloader;
import com.bagnoli.verificac19.service.downloaders.SettingsDownloader;

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
    private final DrlChecker drlChecker;
    private final DrlDownloader drlDownloader;

    void onStart(@Observes StartupEvent ev) {
        log.info("Downloading certificates...");
        certificatesDownloader.download();
        log.info("Done downloading certificates.");
        log.info("Downloading settings...");
        settingsDownloader.downloadSettings();
        log.info("Downloading settings...");
        drlChecker.check(0L, 0L);
        drlDownloader.download(0L, 0L);
        log.info("Done downloading drl, application up and running!");
    }
}
