package com.github.alessandrobagnoli.verificac19.service.validationlogic;

import java.util.Optional;

import com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus;

public interface RevokedAndBlacklistedChecker {
    Optional<CertificateStatus> check(String identifier);
}
