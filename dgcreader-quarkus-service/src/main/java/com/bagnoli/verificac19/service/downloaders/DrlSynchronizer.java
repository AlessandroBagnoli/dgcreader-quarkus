package com.bagnoli.verificac19.service.downloaders;

public interface DrlSynchronizer {
    void synchronize(Long version, Long chunk);
}
