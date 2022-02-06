package com.bagnoli.verificac19.service.validationlogic;

import static com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID;
import static com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID_YET;
import static com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus.TEST_NEEDED;
import static com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus.VALID;
import static com.bagnoli.verificac19.dto.ValidationScanMode.BOOSTER_DGP;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import com.bagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate;
import com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus;
import com.bagnoli.verificac19.dto.ValidationScanMode;
import com.bagnoli.verificac19.exception.EmptyDigitalCovidCertificateException;

import lombok.RequiredArgsConstructor;
import se.digg.dgc.payload.v1.RecoveryEntry;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteRecoveryValidator implements RecoveryValidator {

    private final RevokedAndBlacklistedChecker revokedAndBlacklistedChecker;

    @Override
    public CertificateStatus calculateValidity(
        EnrichedDigitalCovidCertificate digitalCovidCertificate,
        ValidationScanMode validationScanMode) {
        RecoveryEntry recoveryEntry = digitalCovidCertificate.getR().stream()
            .reduce((first, second) -> second)
            .orElseThrow(() -> new EmptyDigitalCovidCertificateException("No recoveries found"));
        String certificateIdentifier = recoveryEntry.getCi();

        Optional<CertificateStatus> check =
            revokedAndBlacklistedChecker.check(certificateIdentifier);
        if (check.isPresent()) {
            return check.get();
        }

        LocalDate certificateValidFrom = recoveryEntry.getDf();
        LocalDate certificateValidUntil = recoveryEntry.getDu();
        LocalDate now = LocalDate.now();

        if (certificateValidFrom.isAfter(now)) {
            return NOT_VALID_YET;
        }

        if (now.isAfter(certificateValidUntil)) {
            return NOT_VALID;
        }

        return validationScanMode == BOOSTER_DGP ? TEST_NEEDED : VALID;
    }
}
