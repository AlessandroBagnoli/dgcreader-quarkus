package com.github.alessandrobagnoli.verificac19.service.downloaders;

import static com.github.alessandrobagnoli.verificac19.utility.CertificateUtilities.convertToX509Cert;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import com.github.alessandrobagnoli.verificac19.service.restclient.DGCApiService;

import io.quarkus.cache.CacheResult;
import lombok.RequiredArgsConstructor;

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

}
