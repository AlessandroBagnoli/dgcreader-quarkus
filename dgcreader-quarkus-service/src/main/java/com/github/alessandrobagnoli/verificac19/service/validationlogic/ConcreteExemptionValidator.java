package com.github.alessandrobagnoli.verificac19.service.validationlogic;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import com.github.alessandrobagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate;
import com.github.alessandrobagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate.ExemptionEntry;
import com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode;
import com.github.alessandrobagnoli.verificac19.exception.EmptyDigitalCovidCertificateException;
import com.github.alessandrobagnoli.verificac19.dto.GPValidResponse;

import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteExemptionValidator implements ExemptionValidator {

    private final RevokedAndBlacklistedChecker revokedAndBlacklistedChecker;

    @Override
    public GPValidResponse.CertificateStatus calculateValidity(
        EnrichedDigitalCovidCertificate digitalCovidCertificate,
        ValidationScanMode validationScanMode) {
        ExemptionEntry exemptionEntry = digitalCovidCertificate.getE().stream()
            .reduce((first, second) -> second)
            .orElseThrow(() -> new EmptyDigitalCovidCertificateException("No exemptions found"));
        String certificateIdentifier = exemptionEntry.getCi();

        Optional<GPValidResponse.CertificateStatus> check =
            revokedAndBlacklistedChecker.check(certificateIdentifier);
        if (check.isPresent()) {
            return check.get();
        }
        
        LocalDate startDateTime = exemptionEntry.getDf();
        LocalDate endDateTime = exemptionEntry.getDu();
        LocalDate now = LocalDate.now();

        if (startDateTime.isAfter(now)) {
            return GPValidResponse.CertificateStatus.NOT_VALID_YET;
        }

        if (now.isAfter(endDateTime)) {
            return GPValidResponse.CertificateStatus.NOT_VALID;
        }

        return validationScanMode == ValidationScanMode.BOOSTER_DGP ? GPValidResponse.CertificateStatus.TEST_NEEDED : GPValidResponse.CertificateStatus.VALID;
    }

}
