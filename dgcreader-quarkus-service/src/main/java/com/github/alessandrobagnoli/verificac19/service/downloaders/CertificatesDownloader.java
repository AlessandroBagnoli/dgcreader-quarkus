package com.github.alessandrobagnoli.verificac19.service.downloaders;

import java.security.cert.X509Certificate;
import java.util.Map;

@FunctionalInterface
public interface CertificatesDownloader {
    Map<String, X509Certificate> download();
}
