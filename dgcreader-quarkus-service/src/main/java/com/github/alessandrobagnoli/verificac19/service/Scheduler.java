package com.github.alessandrobagnoli.verificac19.service;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

import com.github.alessandrobagnoli.verificac19.service.downloaders.DrlChecker;
import com.github.alessandrobagnoli.verificac19.service.downloaders.DrlSynchronizer;

import io.quarkus.scheduler.Scheduled;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
@Transactional
public class Scheduler {

    private final LockWhileDrlSync lockWhileDrlSync;
    private final DrlChecker drlChecker;
    private final DrlSynchronizer drlSynchronizer;

    @Scheduled(cron = "0 15 3 * * ?")
    void synchronizeDrl() {
        lockWhileDrlSync.lock();
        try {
            //TODO passare valori sensati, accorpare il checker con synchronizer, inutile tenerli separati
            drlChecker.check(0L, 0L);
            drlSynchronizer.synchronize(0L, 0L);
        } finally {
            lockWhileDrlSync.unlock();
        }
    }

}
