package com.github.alessandrobagnoli.verificac19.service.validationlogic;

import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID;
import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID_YET;
import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.TEST_NEEDED;
import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.VALID;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.IT_ENTRY_DGP;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.RSA_VISITORS_DGP;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import com.github.alessandrobagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate;
import com.github.alessandrobagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate.ExemptionEntry;
import com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus;
import com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode;
import com.github.alessandrobagnoli.verificac19.exception.EmptyDigitalCovidCertificateException;

import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteExemptionValidator implements ExemptionValidator {

    private final RevokedAndBlacklistedChecker revokedAndBlacklistedChecker;

    @Override
    public CertificateStatus calculateValidity(
        EnrichedDigitalCovidCertificate digitalCovidCertificate,
        ValidationScanMode validationScanMode) {
        ExemptionEntry exemptionEntry = digitalCovidCertificate.getE().stream()
            .findFirst()
            .orElseThrow(() -> new EmptyDigitalCovidCertificateException("No exemptions found"));
        String certificateIdentifier = exemptionEntry.getCi();

        Optional<CertificateStatus> check =
            revokedAndBlacklistedChecker.check(certificateIdentifier);
        if (check.isPresent()) {
            return check.get();
        }

        LocalDate startDateTime = exemptionEntry.getDf();
        LocalDate endDateTime = exemptionEntry.getDu();
        LocalDate now = LocalDate.now();

        if (startDateTime.isAfter(now)) {
            return NOT_VALID_YET;
        }

        if (now.isAfter(endDateTime)) {
            return NOT_VALID;
        }

        return validationScanMode == IT_ENTRY_DGP ? NOT_VALID : isTestNeeded(validationScanMode);
    }

    private CertificateStatus isTestNeeded(ValidationScanMode validationScanMode) {
        return validationScanMode == RSA_VISITORS_DGP ? TEST_NEEDED : VALID;
    }

}
