package com.bagnoli.verificac19.service.validationlogic;

import static com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID;
import static com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus.REVOKED;
import static com.bagnoli.verificac19.utility.CertificateUtilities.sha256;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import com.bagnoli.verificac19.dao.BlackListedPassDAO;
import com.bagnoli.verificac19.dao.RevokedPassDAO;
import com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus;

import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteRevokedAndBlacklistedChecker implements RevokedAndBlacklistedChecker {

    private final RevokedPassDAO revokedPassDAO;
    private final BlackListedPassDAO blackListedPassDAO;

    @Override
    public Optional<CertificateStatus> check(String identifier) {
        if (revokedPassDAO.findByIdOptional(sha256(identifier)).isPresent()) {
            return Optional.of(REVOKED);
        }
        return blackListedPassDAO.findByIdOptional(identifier).isPresent() ?
            Optional.of(NOT_VALID) :
            Optional.empty();
    }

}
