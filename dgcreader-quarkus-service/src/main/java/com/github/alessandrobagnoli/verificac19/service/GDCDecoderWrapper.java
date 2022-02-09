package com.github.alessandrobagnoli.verificac19.service;

import com.github.alessandrobagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate;

public interface GDCDecoderWrapper {
    EnrichedDigitalCovidCertificate decode(String base45);

    EnrichedDigitalCovidCertificate decode(byte[] file);
}
