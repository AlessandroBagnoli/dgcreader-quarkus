package com.github.alessandrobagnoli.verificac19.service.validationlogic;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import com.github.alessandrobagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate;
import com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode;
import com.github.alessandrobagnoli.verificac19.exception.EmptyDigitalCovidCertificateException;
import com.github.alessandrobagnoli.verificac19.dto.GPValidResponse;

import lombok.RequiredArgsConstructor;
import se.digg.dgc.payload.v1.RecoveryEntry;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteRecoveryValidator implements RecoveryValidator {

    private final RevokedAndBlacklistedChecker revokedAndBlacklistedChecker;

    @Override
    public GPValidResponse.CertificateStatus calculateValidity(
        EnrichedDigitalCovidCertificate digitalCovidCertificate,
        ValidationScanMode validationScanMode) {
        RecoveryEntry recoveryEntry = digitalCovidCertificate.getR().stream()
            .reduce((first, second) -> second)
            .orElseThrow(() -> new EmptyDigitalCovidCertificateException("No recoveries found"));
        String certificateIdentifier = recoveryEntry.getCi();

        Optional<GPValidResponse.CertificateStatus> check =
            revokedAndBlacklistedChecker.check(certificateIdentifier);
        if (check.isPresent()) {
            return check.get();
        }

        LocalDate certificateValidFrom = recoveryEntry.getDf();
        LocalDate certificateValidUntil = recoveryEntry.getDu();
        LocalDate now = LocalDate.now();

        if (certificateValidFrom.isAfter(now)) {
            return GPValidResponse.CertificateStatus.NOT_VALID_YET;
        }

        if (now.isAfter(certificateValidUntil)) {
            return GPValidResponse.CertificateStatus.NOT_VALID;
        }

        return validationScanMode == ValidationScanMode.BOOSTER_DGP ? GPValidResponse.CertificateStatus.TEST_NEEDED : GPValidResponse.CertificateStatus.VALID;
    }
}
