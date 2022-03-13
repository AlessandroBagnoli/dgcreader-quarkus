package com.github.alessandrobagnoli.verificac19.service;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

import com.github.alessandrobagnoli.verificac19.dao.BlackListedPassDAO;
import com.github.alessandrobagnoli.verificac19.dao.RevokedPassDAO;
import com.github.alessandrobagnoli.verificac19.service.downloaders.CertificatesDownloader;
import com.github.alessandrobagnoli.verificac19.service.downloaders.DrlStatusStore;
import com.github.alessandrobagnoli.verificac19.service.downloaders.DrlSynchronizer;
import com.github.alessandrobagnoli.verificac19.service.downloaders.SettingsDownloader;

import io.quarkus.scheduler.Scheduled;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Transactional
@Slf4j
public class Scheduler {

    private final CertificatesDownloader certificatesDownloader;
    private final SettingsDownloader settingsDownloader;
    private final CacheService cacheService;
    private final LockWhileDrlSync lockWhileDrlSync;
    private final DrlSynchronizer drlSynchronizer;
    private final BlackListedPassDAO blackListedPassDAO;
    private final RevokedPassDAO revokedPassDAO;
    private final DrlStatusStore drlStatusStore;

    @Scheduled(cron = "0 0 3 * * ?")
    void synchronizeDrl() {
        lockWhileDrlSync.lock();
        try {
            log.info("Invalidating cache...");
            cacheService.invalidateAll();
            log.info("Invalidating blacklisted and revoked passes...");
            blackListedPassDAO.deleteAll();
            revokedPassDAO.deleteAll();
            drlStatusStore.resetDrlStatus();
            log.info("Recreating caches...");
            certificatesDownloader.download();
            settingsDownloader.downloadSettings();
            log.info("Downloading drl...");
            drlSynchronizer.synchronize();
            log.info("Done!");
        } finally {
            lockWhileDrlSync.unlock();
        }
    }

}
