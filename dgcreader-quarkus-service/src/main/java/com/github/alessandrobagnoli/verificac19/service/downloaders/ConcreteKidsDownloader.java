package com.github.alessandrobagnoli.verificac19.service.downloaders;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.github.alessandrobagnoli.verificac19.service.DGCApiService;

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
