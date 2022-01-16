package com.bagnoli.verificac19.service;

import se.digg.dgc.payload.v1.DigitalCovidCertificate;

@FunctionalInterface
public interface GDCDecoderWrapper {
    DigitalCovidCertificate decode(String base45);
}
