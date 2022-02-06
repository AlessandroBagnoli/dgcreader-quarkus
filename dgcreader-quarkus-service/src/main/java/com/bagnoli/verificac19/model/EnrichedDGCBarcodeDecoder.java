package com.bagnoli.verificac19.model;

import java.io.IOException;
import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;

import se.digg.dgc.encoding.BarcodeException;
import se.digg.dgc.payload.v1.DGCSchemaException;

public interface EnrichedDGCBarcodeDecoder extends EnrichedDGCDecoder {

    /**
     * Given a barcode image the method verifies amd decodes the contents into its DCC payload representation.
     *
     * @param image the barcode image holding the encoded and signed DCC
     * @return the DCC payload
     * @throws DGCSchemaException          for DCC schema errors
     * @throws SignatureException          for signature verification errors
     * @throws CertificateExpiredException if the DCC has expired
     * @throws BarcodeException            for errors reading the barcode
     * @throws IOException                 for errors decoding data, for example CBOR related errors
     * @see #decodeBarcodeToBytes(byte[])
     */
    EnrichedDigitalCovidCertificate decodeBarcode(final byte[] image)
        throws DGCSchemaException, SignatureException, CertificateExpiredException,
        BarcodeException, IOException;

}
