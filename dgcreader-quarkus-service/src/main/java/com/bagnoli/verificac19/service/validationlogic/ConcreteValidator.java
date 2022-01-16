package com.bagnoli.verificac19.service.validationlogic;

import static java.util.Optional.ofNullable;

import javax.enterprise.context.ApplicationScoped;

import com.bagnoli.verificac19.exception.EmptyDigitalCovidCertificateException;
import com.bagnoli.verificac19.dto.GPValidResponse;

import lombok.RequiredArgsConstructor;
import se.digg.dgc.payload.v1.DigitalCovidCertificate;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteValidator implements Validator {

    private final VaccineValidator vaccineValidator;
    private final TestValidator testValidator;
    private final RecoveryValidator recoveryValidator;

    @Override
    public GPValidResponse validate(DigitalCovidCertificate digitalCovidCertificate) {
        return ofNullable(digitalCovidCertificate.getV())
            .map(x -> vaccineValidator.calculateValidity(digitalCovidCertificate))
            .or(() -> ofNullable(digitalCovidCertificate.getT())
                .map(x -> testValidator.calculateValidity(digitalCovidCertificate)))
            .or(() -> ofNullable(digitalCovidCertificate.getR()).map(
                x -> recoveryValidator.calculateValidity(digitalCovidCertificate)))
            .orElseThrow(() -> new EmptyDigitalCovidCertificateException(
                "Cannot check validity of empty Digital Covid Certificate"));
    }
}
