package com.bagnoli.verificac19.utility;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.apache.commons.codec.binary.Base64;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CertificateUtilities {

    @SneakyThrows
    public static X509Certificate convertToX509Cert(String certificateString) {
        X509Certificate certificate;
        CertificateFactory cf;
        byte[] certificateData = java.util.Base64.getDecoder().decode(certificateString);
        cf = CertificateFactory.getInstance("X509");
        certificate =
            (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateData));
        return certificate;
    }

    @SneakyThrows
    public static String sha256(String value) {
        return hashString(value, "SHA-256");
    }

    private static String hashString(String input, String algorithm)
        throws NoSuchAlgorithmException {
        return encodeBase64(MessageDigest.getInstance(algorithm).digest(input.getBytes()));
    }

    private static String encodeBase64(byte[] input) {
        return Base64.encodeBase64String(input);
    }

}
