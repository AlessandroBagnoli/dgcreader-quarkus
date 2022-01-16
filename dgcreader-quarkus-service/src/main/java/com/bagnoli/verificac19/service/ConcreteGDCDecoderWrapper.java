package com.bagnoli.verificac19.service;

import java.io.IOException;
import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;

import javax.enterprise.context.ApplicationScoped;

import com.bagnoli.verificac19.service.downloaders.CertificatesDownloader;

import lombok.RequiredArgsConstructor;
import se.digg.dgc.payload.v1.DGCSchemaException;
import se.digg.dgc.payload.v1.DigitalCovidCertificate;
import se.digg.dgc.service.DGCDecoder;
import se.digg.dgc.service.impl.DefaultDGCDecoder;
import se.digg.dgc.signatures.impl.DefaultDGCSignatureVerifier;

@RequiredArgsConstructor
@ApplicationScoped
public class ConcreteGDCDecoderWrapper implements GDCDecoderWrapper {

    private final CertificatesDownloader certificatesDownloader;

    @Override
    public DigitalCovidCertificate decode(String base45) {
        DGCDecoder decoder = new DefaultDGCDecoder(new DefaultDGCSignatureVerifier(),
            (x, y) -> certificatesDownloader.download());
        DigitalCovidCertificate digitalCovidCertificate;
        try {
            digitalCovidCertificate = decoder.decode(base45);
        } catch (DGCSchemaException | SignatureException | CertificateExpiredException | IOException e) {
            throw new RuntimeException();
        }

        return digitalCovidCertificate;

    }
}
