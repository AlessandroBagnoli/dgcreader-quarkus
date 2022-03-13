package com.github.alessandrobagnoli.verificac19.service.downloaders;

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;

import com.github.alessandrobagnoli.verificac19.dao.RevokedPassDAO;
import com.github.alessandrobagnoli.verificac19.dto.CertificateRevocationList;
import com.github.alessandrobagnoli.verificac19.dto.CrlStatus;
import com.github.alessandrobagnoli.verificac19.model.RevokedPass;
import com.github.alessandrobagnoli.verificac19.service.DGCApiService;
import com.github.alessandrobagnoli.verificac19.service.downloaders.DrlStatusStore.DrlStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ConcreteDrlSynchronizer implements DrlSynchronizer {

    private final DGCApiService dgcApiService;
    private final DrlStatusStore drlStatusStore;
    private final RevokedPassDAO revokedPassDAO;

    @Override
    public void synchronize() {
        evaluateSync();
        postSyncCheck();
    }

    private void evaluateSync() {
        DrlStatus drlStatus = drlStatusStore.getDrlStatus();
        Long inStoreVersion = drlStatus.getVersion();
        long chunk = 0L;
        CrlStatus preSync = dgcApiService.getCRLStatus(inStoreVersion, chunk);
        Long preSyncVersion = preSync.getVersion();
        if (preSyncVersion > inStoreVersion) {
            performSync(inStoreVersion, chunk);
        }
    }

    private void performSync(Long version, long chunk) {
        boolean doWhile = true;
        while (doWhile) {
            CertificateRevocationList certificateRevocationList =
                dgcApiService.getCertificateRevocationList(version, chunk);
            ofNullable(certificateRevocationList.getDelta()).ifPresentOrElse(handleDiff(),
                handleSnapshot(certificateRevocationList.getRevokedUcvi()));
            version = certificateRevocationList.getFromVersion();
            chunk = certificateRevocationList.getChunk() + 1;
            doWhile =
                certificateRevocationList.getChunk() < certificateRevocationList.getLastChunk();
        }
        log.info("I have " + revokedPassDAO.count() + " revoked passes saved");
    }

    private void postSyncCheck() {
        CrlStatus postSync = dgcApiService.getCRLStatus(0L, 0L);
        if (postSync.getTotalNumberUCVI().equals(revokedPassDAO.count())) {
            drlStatusStore.setDrlStatus(DrlStatus.builder()
                .version(postSync.getVersion())
                .build());
        } else {
            revokedPassDAO.deleteAll();
            drlStatusStore.resetDrlStatus();
            synchronize();
        }
    }

    private Consumer<CertificateRevocationList.Delta> handleDiff() {
        return delta -> {
            ofNullable(delta.getDeletions()).ifPresent(handleDeletions());
            ofNullable(delta.getInsertions()).ifPresent(handleInsertions());
        };
    }

    private Consumer<List<String>> handleDeletions() {
        return deletions -> deletions.forEach(revokedPassDAO::deleteById);
    }

    private Consumer<List<String>> handleInsertions() {
        return insertions -> insertions.forEach(s ->
            revokedPassDAO.persist(RevokedPass.builder()
                .hashedUVCI(s)
                .build()));
    }

    private Runnable handleSnapshot(List<String> revokedUcvis) {
        return () -> {
            List<RevokedPass> toAdd = new ArrayList<>();
            revokedUcvis.forEach(s -> toAdd.add(RevokedPass.builder().hashedUVCI(s).build()));
            revokedPassDAO.persist(toAdd);
        };
    }
}
