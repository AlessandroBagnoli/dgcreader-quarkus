package com.github.alessandrobagnoli.verificac19.service.downloaders;

import java.security.cert.X509Certificate;
import java.util.List;

@FunctionalInterface
public interface CertificatesDownloader {
    List<X509Certificate> download();
}
