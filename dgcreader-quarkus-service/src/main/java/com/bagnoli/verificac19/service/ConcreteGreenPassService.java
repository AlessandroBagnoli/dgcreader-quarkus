package com.bagnoli.verificac19.service;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

import com.bagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate;
import com.bagnoli.verificac19.dto.GPValidResponse;
import com.bagnoli.verificac19.dto.ValidationScanMode;
import com.bagnoli.verificac19.service.validationlogic.Validator;

import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
@Transactional
public class ConcreteGreenPassService implements GreenPassService {

    private final GDCDecoderWrapper gdcDecoderWrapper;
    private final Validator validator;

    @Override
    public GPValidResponse validate(String base45, ValidationScanMode validationScanMode) {
        EnrichedDigitalCovidCertificate digitalCovidCertificate = gdcDecoderWrapper.decode(base45);
        return validator.validate(digitalCovidCertificate, validationScanMode);
    }

    @Override
    public GPValidResponse validate(byte[] file, ValidationScanMode validationScanMode) {
        EnrichedDigitalCovidCertificate digitalCovidCertificate = gdcDecoderWrapper.decode(file);
        return validator.validate(digitalCovidCertificate, validationScanMode);
    }

}
