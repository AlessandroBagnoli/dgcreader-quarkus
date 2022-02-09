package com.github.alessandrobagnoli.verificac19.service.validationlogic;

import com.github.alessandrobagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate;
import com.github.alessandrobagnoli.verificac19.dto.GPValidResponse;
import com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode;

public interface Validator {
    GPValidResponse validate(EnrichedDigitalCovidCertificate digitalCovidCertificate,
        ValidationScanMode validationScanMode);
}
