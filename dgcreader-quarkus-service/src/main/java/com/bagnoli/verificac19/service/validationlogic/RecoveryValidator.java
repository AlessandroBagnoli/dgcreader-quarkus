package com.bagnoli.verificac19.service.validationlogic;

import com.bagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate;
import com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus;
import com.bagnoli.verificac19.dto.ValidationScanMode;

public interface RecoveryValidator {
    CertificateStatus calculateValidity(EnrichedDigitalCovidCertificate digitalCovidCertificate,
        ValidationScanMode validationScanMode);
}
