package com.github.alessandrobagnoli.verificac19.service;

import java.security.cert.X509Certificate;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.arc.Lock;
import lombok.Getter;
import lombok.Setter;

@ApplicationScoped
@Lock
@Getter(onMethod_ = {@Lock(value = Lock.Type.READ)})
@Setter
public class CurrentCertificateStore implements CertificateStore {

    private X509Certificate certificate;

}
