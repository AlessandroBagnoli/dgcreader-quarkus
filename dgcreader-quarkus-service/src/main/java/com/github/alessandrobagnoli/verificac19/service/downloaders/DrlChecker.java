package com.github.alessandrobagnoli.verificac19.service.downloaders;

import com.github.alessandrobagnoli.verificac19.dto.CrlStatus;

@FunctionalInterface
public interface DrlChecker {
    CrlStatus check(Long version, Long chunk);
}
