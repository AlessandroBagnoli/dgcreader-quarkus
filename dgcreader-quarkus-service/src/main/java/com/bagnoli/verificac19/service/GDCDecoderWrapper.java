package com.bagnoli.verificac19.service;

import com.bagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate;

public interface GDCDecoderWrapper {
    EnrichedDigitalCovidCertificate decode(String base45);

    EnrichedDigitalCovidCertificate decode(byte[] file);
}
