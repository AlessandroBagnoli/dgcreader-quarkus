package com.github.alessandrobagnoli.verificac19.customdecoder;

import java.io.IOException;
import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;

import se.digg.dgc.payload.v1.DGCSchemaException;
import se.digg.dgc.service.DGCDecoder;

public interface EnrichedDGCDecoder extends DGCDecoder {

    /**
     * Given the base45-encoding of a signed DCC the method verifies and decodes it into the DCC payload representation.
     *
     * @param base45 the base45-encoding of the signed DCC (including the HCERT header)
     * @return the DCC payload
     * @throws DGCSchemaException          for DCC schema errors
     * @throws SignatureException          for signature verification errors
     * @throws CertificateExpiredException if the DCC has expired
     * @throws IOException                 for errors decoding data, for example CBOR related errors
     * @see #decodeBarcodeToBytes(byte[])
     */
    EnrichedDigitalCovidCertificate decode(final String base45)
        throws DGCSchemaException, SignatureException, CertificateExpiredException, IOException;

    /**
     * Verifies a "raw" DCC (i.e., a signed CWT holding the DCC payload) and decodes it to the actual DCC payload.
     *
     * @param cwt the signed CWT holding the DCC
     * @return the DCC payload
     * @throws DGCSchemaException          for DCC schema errors
     * @throws SignatureException          for signature verification errors
     * @throws CertificateExpiredException if the DCC has expired
     * @throws IOException                 for errors decoding data, for example CBOR related errors
     * @see #decodeRawToBytes(byte[])
     */
    EnrichedDigitalCovidCertificate decodeRaw(final byte[] cwt)
        throws DGCSchemaException, SignatureException, CertificateExpiredException, IOException;

}
