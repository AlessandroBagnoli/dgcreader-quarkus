package com.github.alessandrobagnoli.verificac19.service;

import java.security.cert.X509Certificate;

public interface CertificateStore {

    X509Certificate getCertificate();

    void setCertificate(X509Certificate certificate);

}
