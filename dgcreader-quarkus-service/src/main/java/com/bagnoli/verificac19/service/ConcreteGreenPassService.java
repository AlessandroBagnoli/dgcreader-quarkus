package com.bagnoli.verificac19.service;

import java.security.cert.Certificate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import com.bagnoli.verificac19.dto.GPValidResponse;
import com.bagnoli.verificac19.dto.Setting;
import com.bagnoli.verificac19.service.downloaders.CertificatesDownloader;
import com.bagnoli.verificac19.service.downloaders.KidsDownloader;
import com.bagnoli.verificac19.service.downloaders.SettingsDownloader;
import com.bagnoli.verificac19.service.validationlogic.Validator;

import lombok.RequiredArgsConstructor;
import se.digg.dgc.payload.v1.DigitalCovidCertificate;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteGreenPassService implements GreenPassService {

    private final GDCDecoderWrapper gdcDecoderWrapper;
    private final CertificatesDownloader certificatesDownloader;
    private final SettingsDownloader settingsDownloader;
    private final KidsDownloader kidsDownloader;
    private final Validator validator;

    @Override
    public GPValidResponse validate(String base45, boolean includeValidUntil) {
        DigitalCovidCertificate digitalCovidCertificate = gdcDecoderWrapper.decode(base45);
        GPValidResponse gpValidResponse = validator.validate(digitalCovidCertificate);
        gpValidResponse.setValidUntil(includeValidUntil ? gpValidResponse.getValidUntil() : null);
        return gpValidResponse;
    }

    @Override
    public Set<Setting> getSettings() {
        return settingsDownloader.downloadSettings();
    }

    @Override
    public Set<String> getCertificates() {
        return certificatesDownloader.download()
            .stream()
            .map(Certificate::toString)
            .collect(Collectors.toSet());
    }

    @Override
    public List<String> getKids() {
        return kidsDownloader.download();
    }

}
