package com.bagnoli.verificac19.service.downloaders;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.cache.CacheResult;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteCertificatesDownloader implements CertificatesDownloader {

    @ConfigProperty(name = "certificates-update-api.url")
    String certificatesUpdateUrl;
    
    private final KidsDownloader kidsDownloader;
    private final HttpClient httpClient;

    @Override
    public List<X509Certificate> download() {
        List<X509Certificate> certificates;
        try {
            certificates = new ArrayList<>(getCertificates());
        } catch (IOException | CertificateException e) {
            throw new RuntimeException();
        }
        return certificates;
    }

    @CacheResult(cacheName = "certificates-cache")
    List<X509Certificate> getCertificates()
        throws IOException,
        CertificateException {
        List<String> validKids = kidsDownloader.download();
        List<X509Certificate> signerCertificates = new ArrayList<>();
        HttpGet httpGet = new HttpGet(certificatesUpdateUrl);
        boolean doWhile = true;
        do {
            HttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                httpGet.setHeader("X-RESUME-TOKEN",
                    response.getHeaders("X-RESUME-TOKEN")[0].getValue());
                String certificateKid = response.getHeaders("X-KID")[0].getValue();
                if (validKids.contains(certificateKid)) {
                    String certificate = EntityUtils.toString(response.getEntity());
                    signerCertificates.add(convertToX509Cert(certificate));
                }
            } else {
                doWhile = false;
            }
        } while (doWhile);

        return signerCertificates;
    }

    private X509Certificate convertToX509Cert(String certificateString)
        throws CertificateException {
        X509Certificate certificate;
        CertificateFactory cf;
        byte[] certificateData = Base64.getDecoder().decode(certificateString);
        cf = CertificateFactory.getInstance("X509");
        certificate =
            (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateData));
        return certificate;
    }
}
