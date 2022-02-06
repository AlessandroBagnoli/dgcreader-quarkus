package com.bagnoli.verificac19.service.downloaders;

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import com.bagnoli.verificac19.dao.RevokedPassDAO;
import com.bagnoli.verificac19.dto.CertificateRevocationList;
import com.bagnoli.verificac19.dto.CertificateRevocationList.Delta;
import com.bagnoli.verificac19.model.RevokedPass;
import com.bagnoli.verificac19.service.restclient.DGCApiService;

import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteDrlSynchronizer implements DrlSynchronizer {

    private final DGCApiService dgcApiService;
    private final RevokedPassDAO revokedPassDAO;

    @Override
    public void synchronize(Long version, Long chunk) {
        boolean doWhile = true;
        while (doWhile) {
            CertificateRevocationList certificateRevocationList =
                dgcApiService.getCertificateRevocationList(version, chunk);
            List<RevokedPass> toAdd = new ArrayList<>();
            certificateRevocationList.getRevokedUcvi()
                .forEach(s -> toAdd.add(RevokedPass.builder().hashedUVCI(s).build()));
            revokedPassDAO.persist(toAdd);
            ofNullable(certificateRevocationList.getDelta()).ifPresent(this::handleDelta);
            revokedPassDAO.flush();
            doWhile =
                certificateRevocationList.getChunk() < certificateRevocationList.getLastChunk();
            version = certificateRevocationList.getFromVersion();
            chunk = certificateRevocationList.getChunk() + 1;
        }

    }

    private void handleDelta(Delta delta) {
        ofNullable(delta.getDeletions())
            .ifPresent(deletions -> deletions.forEach(revokedPassDAO::deleteById));
        ofNullable(delta.getInsertions())
            .ifPresent(insertions -> insertions.forEach(s ->
                revokedPassDAO.persist(RevokedPass.builder().hashedUVCI(s).build())));
    }
}
