package com.github.alessandrobagnoli.verificac19.service;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

import com.github.alessandrobagnoli.verificac19.service.downloaders.DrlSynchronizer;

import io.quarkus.scheduler.Scheduled;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
@Transactional
public class Scheduler {

    private final LockWhileDrlSync lockWhileDrlSync;
    private final DrlSynchronizer drlSynchronizer;

    @Scheduled(cron = "0 0 3 * * ?")
    void synchronizeDrl() {
        lockWhileDrlSync.lock();
        try {
            drlSynchronizer.synchronize();
        } finally {
            lockWhileDrlSync.unlock();
        }
    }

}
