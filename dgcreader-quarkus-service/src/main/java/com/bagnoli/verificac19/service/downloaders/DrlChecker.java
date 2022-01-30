package com.bagnoli.verificac19.service.downloaders;

import com.bagnoli.verificac19.dto.CrlStatus;

@FunctionalInterface
public interface DrlChecker {
    CrlStatus check(Long version, Long chunk);
}
