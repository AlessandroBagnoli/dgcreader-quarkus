package functional;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import com.github.alessandrobagnoli.verificac19.dto.Setting;
import com.github.alessandrobagnoli.verificac19.service.restclient.DGCApiService;

import io.quarkus.test.junit.QuarkusTest;
import lombok.SneakyThrows;

@QuarkusTest
class RestClientTest {

    @Inject
    DGCApiService underTest;

    @Test
    void testGetSettings() {
        // given

        // when
        Set<Setting> settings = underTest.getSettings();

        // then
        assertFalse(settings.isEmpty());
    }

    @Test
    void testGetKids() {
        // given

        // when
        Set<String> kids = underTest.getKids();

        // then
        assertFalse(kids.isEmpty());
    }

    @Test
    @SneakyThrows
    void testGetCertificates() {
        // given
        List<X509Certificate> signerCertificates = new ArrayList<>();

        // when
        Set<String> kids = underTest.getKids();
        boolean doWhile = true;
        String resumeToken = StringUtils.EMPTY;
        do {
            Response response = underTest.getCertificates(resumeToken);
            String kid = response.getHeaderString("X-KID");
            if (kids.contains(kid)) {
                resumeToken = response.getHeaderString("X-RESUME-TOKEN");
                String certificate = response.readEntity(String.class);
                signerCertificates.add(convertToX509Cert(certificate));
            } else {
                doWhile = false;
            }
        } while (doWhile);

        // then
        assertFalse(signerCertificates.isEmpty());
    }

    private X509Certificate convertToX509Cert(String certificateString)
        throws CertificateException {
        X509Certificate certificate;
        CertificateFactory cf;
        byte[] certificateData = Base64.getDecoder().decode(certificateString);
        cf = CertificateFactory.getInstance("X509");
        certificate =
            (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateData));
        return certificate;
    }

}
