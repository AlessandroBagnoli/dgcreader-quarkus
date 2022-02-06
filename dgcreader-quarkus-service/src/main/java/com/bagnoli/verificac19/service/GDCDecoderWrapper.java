package com.bagnoli.verificac19.service;

import com.bagnoli.verificac19.model.EnrichedDigitalCovidCertificate;

@FunctionalInterface
public interface GDCDecoderWrapper {
    EnrichedDigitalCovidCertificate decode(String base45);
}
