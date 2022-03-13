package com.github.alessandrobagnoli.verificac19.service.validationlogic;

import com.github.alessandrobagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate;
import com.github.alessandrobagnoli.verificac19.dto.GPValidResponse;
import com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode;

public interface RecoveryValidator {
    GPValidResponse.CertificateStatus calculateValidity(
        EnrichedDigitalCovidCertificate digitalCovidCertificate,
        ValidationScanMode validationScanMode);
}
