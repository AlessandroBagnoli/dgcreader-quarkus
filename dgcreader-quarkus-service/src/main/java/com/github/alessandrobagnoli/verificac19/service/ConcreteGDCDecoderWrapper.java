package com.github.alessandrobagnoli.verificac19.service;

import java.io.IOException;
import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;

import javax.enterprise.context.ApplicationScoped;

import com.github.alessandrobagnoli.verificac19.customdecoder.ConcreteEnrichedDGCBarcodeDecoder;
import com.github.alessandrobagnoli.verificac19.customdecoder.EnrichedDGCBarcodeDecoder;
import com.github.alessandrobagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate;
import com.github.alessandrobagnoli.verificac19.exception.ServiceException;
import com.github.alessandrobagnoli.verificac19.service.downloaders.CertificatesDownloader;

import lombok.RequiredArgsConstructor;
import se.digg.dgc.encoding.BarcodeException;
import se.digg.dgc.payload.v1.DGCSchemaException;
import se.digg.dgc.signatures.impl.DefaultDGCSignatureVerifier;

@RequiredArgsConstructor
@ApplicationScoped
public class ConcreteGDCDecoderWrapper implements GDCDecoderWrapper {

    private final CertificatesDownloader certificatesDownloader;

    @Override
    public EnrichedDigitalCovidCertificate decode(String base45) {
        EnrichedDGCBarcodeDecoder decoder =
            new ConcreteEnrichedDGCBarcodeDecoder(new DefaultDGCSignatureVerifier(),
                (x, y) -> certificatesDownloader.download());
        EnrichedDigitalCovidCertificate digitalCovidCertificate;
        try {
            digitalCovidCertificate = decoder.decode(base45);
        } catch (DGCSchemaException | SignatureException | CertificateExpiredException | IOException e) {
            throw new ServiceException(e.getMessage());
        }

        return digitalCovidCertificate;

    }

    @Override
    public EnrichedDigitalCovidCertificate decode(byte[] file) {
        EnrichedDGCBarcodeDecoder decoder =
            new ConcreteEnrichedDGCBarcodeDecoder(new DefaultDGCSignatureVerifier(),
                (x, y) -> certificatesDownloader.download());
        EnrichedDigitalCovidCertificate digitalCovidCertificate;
        try {
            digitalCovidCertificate = decoder.decodeBarcode(file);
        } catch (DGCSchemaException | SignatureException | CertificateExpiredException | IOException | BarcodeException e) {
            throw new ServiceException(e.getMessage());
        }

        return digitalCovidCertificate;
    }
}
