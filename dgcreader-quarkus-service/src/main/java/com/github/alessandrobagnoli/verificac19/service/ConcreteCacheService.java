package com.github.alessandrobagnoli.verificac19.service;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.cache.CacheInvalidateAll;

@ApplicationScoped
public class ConcreteCacheService implements CacheService {

    @Override
    public void invalidateAll() {
        this.invalidateSettings();
        this.invalidateCertificates();
        this.invalidateKids();
    }

    @CacheInvalidateAll(cacheName = "settings-cache")
    void invalidateSettings() {
        // used only to invalidate settings cache
    }

    @CacheInvalidateAll(cacheName = "certificates-cache")
    void invalidateCertificates() {
        // used only to invalidate certificates cache
    }

    @CacheInvalidateAll(cacheName = "kids-cache")
    void invalidateKids() {
        // used only to invalidate kids cache
    }

}
