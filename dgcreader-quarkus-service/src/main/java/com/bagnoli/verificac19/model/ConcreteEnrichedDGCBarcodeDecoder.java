package com.bagnoli.verificac19.model;

import java.io.IOException;
import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;

import lombok.extern.slf4j.Slf4j;
import se.digg.dgc.encoding.BarcodeException;
import se.digg.dgc.encoding.impl.DefaultBarcodeDecoder;
import se.digg.dgc.payload.v1.DGCSchemaException;
import se.digg.dgc.service.impl.DefaultDGCBarcodeDecoder;
import se.digg.dgc.signatures.CertificateProvider;
import se.digg.dgc.signatures.DGCSignatureVerifier;

@Slf4j
public class ConcreteEnrichedDGCBarcodeDecoder extends DefaultDGCBarcodeDecoder implements EnrichedDGCBarcodeDecoder {

    /**
     * Constructor.
     *  @param dgcSignatureVerifier the signature verifier - if null, an instance of {@link DefaultDGCSignatureVerifier} will be used
     * @param certificateProvider  the certificate provider that is used to locate certificates to use when verifying signatures
     */
    public ConcreteEnrichedDGCBarcodeDecoder(DGCSignatureVerifier dgcSignatureVerifier,
        CertificateProvider certificateProvider) {
        super(dgcSignatureVerifier, certificateProvider, new DefaultBarcodeDecoder());
    }

    @Override
    public EnrichedDigitalCovidCertificate decode(String base45)
        throws DGCSchemaException, SignatureException, CertificateExpiredException, IOException {

        final byte[] dccEncoding = this.decodeToBytes(base45);

        log.trace("CBOR decoding DCC ...");
        final EnrichedDigitalCovidCertificate dcc =
            EnrichedDigitalCovidCertificate.decode(dccEncoding);
        log.trace("Decoded into: {}", dcc);

        return dcc;
    }

    @Override
    public EnrichedDigitalCovidCertificate decodeRaw(byte[] cwt)
        throws DGCSchemaException, SignatureException, CertificateExpiredException, IOException {
        final byte[] encodedDcc = this.decodeRawToBytes(cwt);

        log.trace("CBOR decoding DCC ...");
        final EnrichedDigitalCovidCertificate dcc =
            EnrichedDigitalCovidCertificate.decode(encodedDcc);
        log.trace("Decoded into: {}", dcc);

        return dcc;
    }

    @Override
    public EnrichedDigitalCovidCertificate decodeBarcode(byte[] image) throws DGCSchemaException,
        SignatureException, CertificateExpiredException, BarcodeException, IOException {

        final byte[] encodedDcc = this.decodeBarcodeToBytes(image);

        log.trace("CBOR decoding DGC ...");
        final EnrichedDigitalCovidCertificate dgc =
            EnrichedDigitalCovidCertificate.decode(encodedDcc);
        log.trace("Decoded into: {}", dgc);
        return dgc;
    }
}
