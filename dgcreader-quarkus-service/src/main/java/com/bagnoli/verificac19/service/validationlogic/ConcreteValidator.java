package com.bagnoli.verificac19.service.validationlogic;

import static com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_EU_DCC;
import static java.util.Optional.ofNullable;

import javax.enterprise.context.ApplicationScoped;

import com.bagnoli.verificac19.dto.GPValidResponse;
import com.bagnoli.verificac19.dto.GPValidResponse.PersonData;
import com.bagnoli.verificac19.dto.ValidationScanMode;
import com.bagnoli.verificac19.model.EnrichedDigitalCovidCertificate;

import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteValidator implements Validator {

    private final VaccineValidator vaccineValidator;
    private final TestValidator testValidator;
    private final RecoveryValidator recoveryValidator;
    private final ExemptionValidator exemptionValidator;

    @Override
    public GPValidResponse validate(EnrichedDigitalCovidCertificate dgc,
        ValidationScanMode scanMode) {
        PersonData personData = PersonData.builder()
            .name(dgc.getNam().getGn())
            .surname(dgc.getNam().getFn())
            .birthDate(dgc.getDateOfBirth().asLocalDate())
            .build();

        return GPValidResponse.builder()
            .personData(personData)
            .certificateStatus(ofNullable(dgc.getV())
                .map(x -> vaccineValidator.calculateValidity(dgc, scanMode))
                .or(() -> ofNullable(dgc.getT())
                    .map(x -> testValidator.calculateValidity(dgc, scanMode)))
                .or(() -> ofNullable(dgc.getR())
                    .map(x -> recoveryValidator.calculateValidity(dgc, scanMode)))
                .or(() -> ofNullable(dgc.getE())
                    .map(x -> exemptionValidator.calculateValidity(dgc, scanMode)))
                .orElse(NOT_EU_DCC))
            .build();
    }
}
