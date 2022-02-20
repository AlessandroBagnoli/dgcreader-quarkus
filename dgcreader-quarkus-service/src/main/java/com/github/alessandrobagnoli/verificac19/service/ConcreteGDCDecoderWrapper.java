package com.github.alessandrobagnoli.verificac19.service;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import java.io.IOException;
import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.codec.binary.Base64;

import com.github.alessandrobagnoli.verificac19.customdecoder.ConcreteEnrichedDGCBarcodeDecoder;
import com.github.alessandrobagnoli.verificac19.customdecoder.EnrichedDGCBarcodeDecoder;
import com.github.alessandrobagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate;
import com.github.alessandrobagnoli.verificac19.exception.ServiceException;
import com.github.alessandrobagnoli.verificac19.service.downloaders.CertificatesDownloader;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import se.digg.dgc.encoding.BarcodeException;
import se.digg.dgc.payload.v1.DGCSchemaException;
import se.digg.dgc.signatures.CertificateProvider;
import se.digg.dgc.signatures.impl.DefaultDGCSignatureVerifier;

@RequiredArgsConstructor
@ApplicationScoped
public class ConcreteGDCDecoderWrapper implements GDCDecoderWrapper {

    private final CertificatesDownloader certificatesDownloader;
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private X509Certificate currentCertificate;

    @Override
    public EnrichedDigitalCovidCertificate decode(String base45) {
        EnrichedDGCBarcodeDecoder decoder =
            createDecoder();
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
        EnrichedDGCBarcodeDecoder decoder = createDecoder();
        EnrichedDigitalCovidCertificate digitalCovidCertificate;
        try {
            digitalCovidCertificate = decoder.decodeBarcode(file);
        } catch (DGCSchemaException | SignatureException | CertificateExpiredException | IOException | BarcodeException e) {
            throw new ServiceException(e.getMessage());
        }

        return digitalCovidCertificate;
    }

    private EnrichedDGCBarcodeDecoder createDecoder() {
        CertificateProvider certificateProvider =
            (country, kid) -> {
                String base64Kid = Base64.encodeBase64String(kid);
                X509Certificate cert = certificatesDownloader.download().get(base64Kid);
                setCurrentCertificate(cert);
                return ofNullable(cert)
                    .map(Collections::singletonList)
                    .orElse(emptyList());
            };
        return new ConcreteEnrichedDGCBarcodeDecoder(new DefaultDGCSignatureVerifier(),
            certificateProvider);
    }
    
}
