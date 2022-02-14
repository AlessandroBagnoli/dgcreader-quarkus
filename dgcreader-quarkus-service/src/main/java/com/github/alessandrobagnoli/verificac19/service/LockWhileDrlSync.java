package com.github.alessandrobagnoli.verificac19.service;

public interface LockWhileDrlSync {
    void lock();

    void unlock();

    boolean isLocked();
}
