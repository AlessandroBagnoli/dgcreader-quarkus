package com.bagnoli.verificac19.service.downloaders;

import java.util.Set;

@FunctionalInterface
public interface KidsDownloader {
    Set<String> download();
}
