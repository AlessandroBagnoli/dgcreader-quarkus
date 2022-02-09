package com.github.alessandrobagnoli.verificac19.service.validationlogic;

import com.github.alessandrobagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate;
import com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode;
import com.github.alessandrobagnoli.verificac19.dto.GPValidResponse;

public interface TestValidator {
    GPValidResponse.CertificateStatus calculateValidity(EnrichedDigitalCovidCertificate digitalCovidCertificate,
        ValidationScanMode validationScanMode);
}
