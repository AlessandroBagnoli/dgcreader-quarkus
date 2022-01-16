package com.bagnoli.verificac19.service.validationlogic;

import java.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;

import com.bagnoli.verificac19.dto.GPValidResponse;
import com.bagnoli.verificac19.dto.GPValidResponse.PersonData;

import lombok.RequiredArgsConstructor;
import se.digg.dgc.payload.v1.DigitalCovidCertificate;
import se.digg.dgc.payload.v1.RecoveryEntry;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteRecoveryValidator implements RecoveryValidator {

    @Override
    public GPValidResponse calculateValidity(DigitalCovidCertificate digitalCovidCertificate) {
        RecoveryEntry recoveryEntry =
            digitalCovidCertificate.getR().stream().findFirst().orElseThrow();

        PersonData personData = PersonData.builder()
            .name(digitalCovidCertificate.getNam().getGn())
            .surname(digitalCovidCertificate.getNam().getFn())
            .birthDate(digitalCovidCertificate.getDateOfBirth().asLocalDate())
            .build();
        LocalDate certificateValidFrom = recoveryEntry.getDf();
        LocalDate certificateValidUntil = recoveryEntry.getDu();
        LocalDate now = LocalDate.now();

        if (certificateValidFrom.isAfter(now)) {
            return GPValidResponse.builder()
                .valid(false)
                .errorDescription("Not valid yet")
                .personData(personData)
                .build();
        }

        if (now.isAfter(certificateValidUntil)) {
            return GPValidResponse.builder()
                .valid(false)
                .errorDescription("Expired")
                .personData(personData)
                .build();
        }

        return GPValidResponse.builder()
            .valid(true)
            .personData(personData)
            .validUntil(certificateValidUntil)
            .build();
    }
}
