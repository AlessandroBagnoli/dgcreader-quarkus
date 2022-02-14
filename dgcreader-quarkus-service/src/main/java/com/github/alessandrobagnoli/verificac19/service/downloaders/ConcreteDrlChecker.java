package com.github.alessandrobagnoli.verificac19.service.downloaders;

import javax.enterprise.context.ApplicationScoped;

import com.github.alessandrobagnoli.verificac19.dto.CrlStatus;
import com.github.alessandrobagnoli.verificac19.service.DGCApiService;

import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteDrlChecker implements DrlChecker {

    private final DGCApiService dgcApiService;

    @Override
    public CrlStatus check(Long version, Long chunk) {
        return dgcApiService.getCRLStatus(version, chunk);
    }

}
