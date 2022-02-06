package com.bagnoli.verificac19.service.validationlogic;

import static com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID;
import static com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID_YET;
import static com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus.TEST_NEEDED;
import static com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus.VALID;
import static com.bagnoli.verificac19.dto.ValidationScanMode.BOOSTER_DGP;

import java.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;

import com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus;
import com.bagnoli.verificac19.dto.ValidationScanMode;
import com.bagnoli.verificac19.exception.EmptyDigitalCovidCertificateException;
import com.bagnoli.verificac19.model.EnrichedDigitalCovidCertificate;
import com.bagnoli.verificac19.model.EnrichedDigitalCovidCertificate.ExemptionEntry;

@ApplicationScoped
public class ConcreteExemptionValidator implements ExemptionValidator {

    @Override
    public CertificateStatus calculateValidity(
        EnrichedDigitalCovidCertificate digitalCovidCertificate,
        ValidationScanMode validationScanMode) {
        ExemptionEntry exemptionEntry = digitalCovidCertificate.getE().stream()
            .reduce((first, second) -> second)
            .orElseThrow(() -> new EmptyDigitalCovidCertificateException("No exemptions found"));
        LocalDate startDateTime = exemptionEntry.getDf();
        LocalDate endDateTime = exemptionEntry.getDu();
        LocalDate now = LocalDate.now();

        if (startDateTime.isAfter(now)) {
            return NOT_VALID_YET;
        }

        if (now.isAfter(endDateTime)) {
            return NOT_VALID;
        }

        return validationScanMode == BOOSTER_DGP ? TEST_NEEDED : VALID;
    }

}
