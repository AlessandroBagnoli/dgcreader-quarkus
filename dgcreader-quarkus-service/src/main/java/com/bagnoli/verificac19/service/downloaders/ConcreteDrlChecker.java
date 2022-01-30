package com.bagnoli.verificac19.service.downloaders;

import javax.enterprise.context.ApplicationScoped;

import com.bagnoli.verificac19.dto.CrlStatus;
import com.bagnoli.verificac19.service.restclient.DGCApiService;

import io.quarkus.cache.CacheResult;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteDrlChecker implements DrlChecker {

    private final DGCApiService dgcApiService;

    @Override
    public CrlStatus check(Long version, Long chunk) {
        return downloadStatus(version, chunk);
    }

    @CacheResult(cacheName = "crl-status-cache")
    CrlStatus downloadStatus(Long version, Long chunk) {
        return dgcApiService.getCRLStatus(version, chunk);
    }

}
