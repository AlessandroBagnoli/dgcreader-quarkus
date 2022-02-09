package com.bagnoli.verificac19.service.downloaders;

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;

import com.bagnoli.verificac19.dao.RevokedPassDAO;
import com.bagnoli.verificac19.dto.CertificateRevocationList;
import com.bagnoli.verificac19.dto.CertificateRevocationList.Delta;
import com.bagnoli.verificac19.model.RevokedPass;
import com.bagnoli.verificac19.service.restclient.DGCApiService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ConcreteDrlSynchronizer implements DrlSynchronizer {

    private final DGCApiService dgcApiService;
    private final RevokedPassDAO revokedPassDAO;

    @Override
    public void synchronize(Long version, Long chunk) {
        boolean doWhile = true;
        while (doWhile) {
            CertificateRevocationList certificateRevocationList =
                dgcApiService.getCertificateRevocationList(version, chunk);
            ofNullable(certificateRevocationList.getDelta()).ifPresentOrElse(handleDiff(),
                handleSnapshot(certificateRevocationList.getRevokedUcvi()));
            doWhile =
                certificateRevocationList.getChunk() < certificateRevocationList.getLastChunk();
            version = certificateRevocationList.getFromVersion();
            chunk = certificateRevocationList.getChunk() + 1;
        }
        log.info("I have " + revokedPassDAO.count() + " revoked passes saved");
        //TODO salvarsi ultima version e chunk a cui si è arrivati da qualche parte, trovare pure il modo per schedulare questa attività in modo tale che venga fatta ad intervalli regolari

    }

    private Consumer<Delta> handleDiff() {
        return delta -> {
            ofNullable(delta.getDeletions())
                .ifPresent(deletions -> deletions.forEach(revokedPassDAO::deleteById));
            ofNullable(delta.getInsertions())
                .ifPresent(insertions -> insertions.forEach(s ->
                    revokedPassDAO.persist(RevokedPass.builder()
                        .hashedUVCI(s)
                        .build())));
        };
    }

    private Runnable handleSnapshot(List<String> revokedUcvis) {
        return () -> {
            List<RevokedPass> toAdd = new ArrayList<>();
            revokedUcvis.forEach(s -> toAdd.add(RevokedPass.builder().hashedUVCI(s).build()));
            revokedPassDAO.persist(toAdd);
            revokedPassDAO.flush();
        };
    }
}
