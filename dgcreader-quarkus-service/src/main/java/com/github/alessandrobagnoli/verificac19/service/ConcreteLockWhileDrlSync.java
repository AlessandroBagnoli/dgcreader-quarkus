package com.github.alessandrobagnoli.verificac19.service;

import java.util.concurrent.Semaphore;

import javax.enterprise.context.ApplicationScoped;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteLockWhileDrlSync implements LockWhileDrlSync {

    private final Semaphore semaphore = new Semaphore(1);

    @SneakyThrows
    @Override
    public void lock() {
        semaphore.acquire();
    }

    @Override
    public void unlock() {
        semaphore.release();
    }
    
    @Override
    public boolean isLocked() {
        return semaphore.availablePermits() == 0;
    }

}
