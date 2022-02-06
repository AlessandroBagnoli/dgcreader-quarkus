package com.bagnoli.verificac19.service.downloaders;

import com.bagnoli.verificac19.dto.CertificateRevocationList;

public interface DrlDownloader {
    CertificateRevocationList download(Long version, Long chunk);
}
