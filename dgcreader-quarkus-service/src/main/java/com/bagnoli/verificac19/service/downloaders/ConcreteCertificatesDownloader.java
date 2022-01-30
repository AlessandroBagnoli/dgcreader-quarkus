package com.bagnoli.verificac19.service.downloaders;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import com.bagnoli.verificac19.service.restclient.DGCApiService;

import io.quarkus.cache.CacheResult;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteCertificatesDownloader implements CertificatesDownloader {

    private final KidsDownloader kidsDownloader;
    private final DGCApiService dgcApiService;

    @Override
    public List<X509Certificate> download() {
        return getCertificates();
    }

    @CacheResult(cacheName = "certificates-cache")
    List<X509Certificate> getCertificates() {
        List<X509Certificate> signerCertificates = new ArrayList<>();
        Set<String> kids = kidsDownloader.download();
        boolean doWhile = true;
        String resumeToken = StringUtils.EMPTY;
        while (doWhile) {
            Response response = dgcApiService.getCertificates(resumeToken);
            String kid = response.getHeaderString("X-KID");
            if (kids.contains(kid)) {
                resumeToken = response.getHeaderString("X-RESUME-TOKEN");
                String certificate = response.readEntity(String.class);
                signerCertificates.add(convertToX509Cert(certificate));
            } else {
                doWhile = false;
            }
        }
        return signerCertificates;
    }

    @SneakyThrows
    private X509Certificate convertToX509Cert(String certificateString) {
        X509Certificate certificate;
        CertificateFactory cf;
        byte[] certificateData = Base64.getDecoder().decode(certificateString);
        cf = CertificateFactory.getInstance("X509");
        certificate =
            (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateData));
        return certificate;
    }
}
