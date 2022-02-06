package com.bagnoli.verificac19.service.validationlogic;

import com.bagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate;
import com.bagnoli.verificac19.dto.GPValidResponse;
import com.bagnoli.verificac19.dto.ValidationScanMode;

public interface Validator {
    GPValidResponse validate(EnrichedDigitalCovidCertificate digitalCovidCertificate,
        ValidationScanMode validationScanMode);
}
