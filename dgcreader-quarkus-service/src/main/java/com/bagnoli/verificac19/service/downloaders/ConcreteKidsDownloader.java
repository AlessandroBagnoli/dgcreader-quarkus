package com.bagnoli.verificac19.service.downloaders;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.bagnoli.verificac19.service.restclient.DGCApiService;

import io.quarkus.cache.CacheResult;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteKidsDownloader implements KidsDownloader {

    private final DGCApiService dgcApiService;

    @Override
    public Set<String> download() {
        return getKids();
    }

    @CacheResult(cacheName = "kids-cache")
    Set<String> getKids() {
        return dgcApiService.getKids();
    }
}
