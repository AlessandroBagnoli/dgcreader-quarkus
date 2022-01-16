package com.bagnoli.verificac19.service.downloaders;

import java.util.List;

@FunctionalInterface
public interface KidsDownloader {
    List<String> download();
}
