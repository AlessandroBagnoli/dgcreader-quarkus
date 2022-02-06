package com.bagnoli.verificac19.service.validationlogic;

import java.util.Optional;

import com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus;

public interface RevokedAndBlacklistedChecker {
    Optional<CertificateStatus> check(String identifier);
}
