package com.bagnoli.verificac19.service.validationlogic;

import static com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID;
import static com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID_YET;
import static com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus.TEST_NEEDED;
import static com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus.VALID;
import static com.bagnoli.verificac19.dto.ValidationScanMode.BOOSTER_DGP;

import java.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;

import com.bagnoli.verificac19.dto.GPValidResponse;
import com.bagnoli.verificac19.dto.GPValidResponse.PersonData;
import com.bagnoli.verificac19.dto.ValidationScanMode;
import com.bagnoli.verificac19.exception.EmptyDigitalCovidCertificateException;

import lombok.RequiredArgsConstructor;
import se.digg.dgc.payload.v1.DigitalCovidCertificate;
import se.digg.dgc.payload.v1.RecoveryEntry;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteRecoveryValidator implements RecoveryValidator {

    @Override
    public GPValidResponse calculateValidity(DigitalCovidCertificate digitalCovidCertificate,
        ValidationScanMode validationScanMode) {
        RecoveryEntry recoveryEntry = digitalCovidCertificate.getR().stream()
            .reduce((first, second) -> second)
            .orElseThrow(() -> new EmptyDigitalCovidCertificateException("No recoveries found"));

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
                .certificateStatus(NOT_VALID_YET)
                .personData(personData)
                .build();
        }

        if (now.isAfter(certificateValidUntil)) {
            return GPValidResponse.builder()
                .certificateStatus(NOT_VALID)
                .personData(personData)
                .build();
        }

        return GPValidResponse.builder()
            .certificateStatus(validationScanMode == BOOSTER_DGP ? TEST_NEEDED : VALID)
            .personData(personData)
            .build();
    }
}
