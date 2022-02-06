package com.bagnoli.verificac19.service.downloaders;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import com.bagnoli.verificac19.dao.RevokedPassDAO;
import com.bagnoli.verificac19.dto.CertificateRevocationList;
import com.bagnoli.verificac19.model.RevokedPass;
import com.bagnoli.verificac19.service.restclient.DGCApiService;

import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteDrlDownloader implements DrlDownloader {

    private final DGCApiService dgcApiService;
    private final RevokedPassDAO revokedPassDAO;

    @Override
    public CertificateRevocationList download(Long version, Long chunk) {
        CertificateRevocationList certificateRevocationList =
            dgcApiService.getCertificateRevocationList(version, chunk);
        List<String> revokedUcvi = certificateRevocationList.getRevokedUcvi();
        List<RevokedPass> revokedPasses = new ArrayList<>();
        revokedUcvi.forEach(s -> revokedPasses.add(RevokedPass.builder().hashedUVCI(s).build()));
        revokedPassDAO.persist(revokedPasses);
        return null;
    }

}
