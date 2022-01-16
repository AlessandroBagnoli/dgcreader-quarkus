package com.bagnoli.verificac19.service.validationlogic;

import com.bagnoli.verificac19.dto.GPValidResponse;

import se.digg.dgc.payload.v1.DigitalCovidCertificate;

public interface RecoveryValidator {
    GPValidResponse calculateValidity(DigitalCovidCertificate digitalCovidCertificate);
}
