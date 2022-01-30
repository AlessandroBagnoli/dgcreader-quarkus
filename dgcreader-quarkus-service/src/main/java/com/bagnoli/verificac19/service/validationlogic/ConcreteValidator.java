package com.bagnoli.verificac19.service.validationlogic;

import static java.util.Optional.ofNullable;

import javax.enterprise.context.ApplicationScoped;

import com.bagnoli.verificac19.dto.GPValidResponse;
import com.bagnoli.verificac19.dto.ValidationScanMode;
import com.bagnoli.verificac19.exception.EmptyDigitalCovidCertificateException;

import lombok.RequiredArgsConstructor;
import se.digg.dgc.payload.v1.DigitalCovidCertificate;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteValidator implements Validator {

    private final VaccineValidator vaccineValidator;
    private final TestValidator testValidator;
    private final RecoveryValidator recoveryValidator;

    @Override
    public GPValidResponse validate(DigitalCovidCertificate digitalCovidCertificate,
        ValidationScanMode validationScanMode) {
        return ofNullable(digitalCovidCertificate.getV())
            .map(x -> vaccineValidator.calculateValidity(digitalCovidCertificate, validationScanMode))
            .or(() -> ofNullable(digitalCovidCertificate.getT())
                .map(x -> testValidator.calculateValidity(digitalCovidCertificate, validationScanMode)))
            .or(() -> ofNullable(digitalCovidCertificate.getR()).map(
                x -> recoveryValidator.calculateValidity(digitalCovidCertificate, validationScanMode)))
            .orElseThrow(() -> new EmptyDigitalCovidCertificateException(
                "Cannot check validity of empty Digital Covid Certificate"));
    }
}
