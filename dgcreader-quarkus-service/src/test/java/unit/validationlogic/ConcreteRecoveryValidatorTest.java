package unit.validationlogic;

import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID;
import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID_YET;
import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.TEST_NEEDED;
import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.VALID;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.BASE_DGP;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.ENHANCED_DGP;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.IT_ENTRY_DGP;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.RSA_VISITORS_DGP;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.WORK_DGP;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import java.math.BigInteger;
import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.alessandrobagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate;
import com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus;
import com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode;
import com.github.alessandrobagnoli.verificac19.exception.EmptyDigitalCovidCertificateException;
import com.github.alessandrobagnoli.verificac19.service.ConcreteGDCDecoderWrapper;
import com.github.alessandrobagnoli.verificac19.service.downloaders.SettingsRetriever;
import com.github.alessandrobagnoli.verificac19.service.validationlogic.ConcreteRecoveryValidator;
import com.github.alessandrobagnoli.verificac19.service.validationlogic.RevokedAndBlacklistedChecker;

import se.digg.dgc.payload.v1.PersonName;
import se.digg.dgc.payload.v1.RecoveryEntry;

@ExtendWith(MockitoExtension.class)
class ConcreteRecoveryValidatorTest {

    private static final String OID_RECOVERY = "1.3.6.1.4.1.1847.2021.1.3";
    private static final String OID_ALT_RECOVERY = "1.3.6.1.4.1.0.1847.2021.1.3";
    private static final String RECOVERY_PV_CERT_START_DAY = "recovery_pv_cert_start_day";
    private static final String RECOVERY_PV_CERT_END_DAY = "recovery_pv_cert_end_day";
    private static final String RECOVERY_CERT_START_DAY_IT = "recovery_cert_start_day_IT";
    private static final String RECOVERY_CERT_END_DAY_IT = "recovery_cert_end_day_IT";
    private static final String RECOVERY_CERT_START_DAY_NOT_IT = "recovery_cert_start_day_NOT_IT";
    private static final String RECOVERY_CERT_END_DAY_NOT_IT = "recovery_cert_end_day_NOT_IT";
    private static final String SETTING_TYPE = "GENERIC";

    private @Mock RevokedAndBlacklistedChecker revokedAndBlacklistedChecker;
    private @Mock SettingsRetriever settingsRetriever;
    private @Mock ConcreteGDCDecoderWrapper decoderWrapper;

    private @InjectMocks ConcreteRecoveryValidator underTest;

    @Test
    void throwsException_when_Empty() {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withR(emptyList());

        // when
        EmptyDigitalCovidCertificateException exception =
            assertThrows(EmptyDigitalCovidCertificateException.class,
                () -> underTest.calculateValidity(dgc, BASE_DGP));

        // then
        assertEquals("No recoveries found", exception.getMessage());
    }

    @Test
    void notValid_when_blacklisted() {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withDob(LocalDate.of(1900, 1, 1))
                .withNam(
                    new PersonName()
                        .withFn("Mario")
                        .withFnt("Mario")
                        .withGn("Rossi")
                        .withGnt("Rossi")
                )
                .withR(singletonList(
                        new RecoveryEntry()
                            .withCo("")
                            .withCi("fakeID")
                            .withIs("")
                            .withTg("")
                            .withDf(null)
                            .withDu(null)
                            .withFr(null)
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(Optional.of(NOT_VALID));

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, BASE_DGP);

        // then
        assertEquals(NOT_VALID, response);
    }

    @Test
    void notValidYet_when_inFuture() {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withDob(LocalDate.of(1900, 1, 1))
                .withNam(
                    new PersonName()
                        .withFn("Mario")
                        .withFnt("Mario")
                        .withGn("Rossi")
                        .withGnt("Rossi")
                )
                .withR(singletonList(
                        new RecoveryEntry()
                            .withCo("")
                            .withCi("fakeID")
                            .withIs("")
                            .withTg("")
                            .withDf(ChronoUnit.DAYS.addTo(LocalDate.now(), 50))
                            .withDu(null)
                            .withFr(null)
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, BASE_DGP);

        // then
        assertEquals(NOT_VALID_YET, response);
    }

    @Test
    void notValid_when_Expired() {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withDob(LocalDate.of(1900, 1, 1))
                .withNam(
                    new PersonName()
                        .withFn("Mario")
                        .withFnt("Mario")
                        .withGn("Rossi")
                        .withGnt("Rossi")
                )
                .withR(singletonList(
                        new RecoveryEntry()
                            .withCo("")
                            .withCi("fakeID")
                            .withIs("")
                            .withTg("")
                            .withDf(ChronoUnit.DAYS.addTo(LocalDate.now(), -50))
                            .withDu(ChronoUnit.DAYS.addTo(LocalDate.now(), -20))
                            .withFr(null)
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, BASE_DGP);

        // then
        assertEquals(NOT_VALID, response);
    }

    @ParameterizedTest
    @MethodSource("createArguments_for_calculateValidity_when_normalRecovery")
    void calculateValidity_when_normalRecovery_certificateNewerThan120days(
        ValidationScanMode validationScanMode, CertificateStatus expected) {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withDob(LocalDate.of(1900, 1, 1))
                .withNam(
                    new PersonName()
                        .withFn("Mario")
                        .withFnt("Mario")
                        .withGn("Rossi")
                        .withGnt("Rossi")
                )
                .withR(singletonList(
                        new RecoveryEntry()
                            .withCo("IT")
                            .withCi("fakeID")
                            .withIs("")
                            .withTg("")
                            .withDf(ChronoUnit.DAYS.addTo(LocalDate.now(), -50))
                            .withDu(ChronoUnit.DAYS.addTo(LocalDate.now(), 200))
                            .withFr(null)
                    )
                );
        X509Certificate certificate = x509CertificateSupplier(singletonList("ignored")).get();
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(decoderWrapper.getCurrentCertificate()).willReturn(certificate);
        given(settingsRetriever.getSettingValue(RECOVERY_CERT_START_DAY_IT, SETTING_TYPE))
            .willReturn(0);
        given(settingsRetriever.getSettingValue(RECOVERY_CERT_END_DAY_IT, SETTING_TYPE))
            .willReturn(Integer.MAX_VALUE);

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, validationScanMode);

        // then
        assertEquals(expected, response);
    }

    @ParameterizedTest
    @MethodSource("createArguments_for_calculateValidity_when_normalRecovery")
    void calculateValidity_when_normalRecovery_certificateOlderThan120days_countryIT(
        ValidationScanMode validationScanMode, CertificateStatus expected) {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withDob(LocalDate.of(1900, 1, 1))
                .withNam(
                    new PersonName()
                        .withFn("Mario")
                        .withFnt("Mario")
                        .withGn("Rossi")
                        .withGnt("Rossi")
                )
                .withR(singletonList(
                        new RecoveryEntry()
                            .withCo("IT")
                            .withCi("fakeID")
                            .withIs("")
                            .withTg("")
                            .withDf(ChronoUnit.DAYS.addTo(LocalDate.now(), -200))
                            .withDu(ChronoUnit.DAYS.addTo(LocalDate.now(), 200))
                            .withFr(null)
                    )
                );
        X509Certificate certificate = x509CertificateSupplier(emptyList()).get();
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(decoderWrapper.getCurrentCertificate()).willReturn(certificate);
        given(settingsRetriever.getSettingValue(RECOVERY_CERT_START_DAY_IT, SETTING_TYPE))
            .willReturn(0);
        given(settingsRetriever.getSettingValue(RECOVERY_CERT_END_DAY_IT, SETTING_TYPE))
            .willReturn(Integer.MAX_VALUE);

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, validationScanMode);

        // then
        assertEquals(expected, response);
    }

    @ParameterizedTest
    @MethodSource("createArguments_for_calculateValidity_when_normalRecovery")
    void calculateValidity_when_normalRecovery_certificateOlderThan120days_and_newerThan180days_countryNOT_IT(
        ValidationScanMode validationScanMode, CertificateStatus expected) {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withDob(LocalDate.of(1900, 1, 1))
                .withNam(
                    new PersonName()
                        .withFn("Mario")
                        .withFnt("Mario")
                        .withGn("Rossi")
                        .withGnt("Rossi")
                )
                .withR(singletonList(
                        new RecoveryEntry()
                            .withCo("NOT_IT")
                            .withCi("fakeID")
                            .withIs("")
                            .withTg("")
                            .withDf(ChronoUnit.DAYS.addTo(LocalDate.now(), -150))
                            .withDu(ChronoUnit.DAYS.addTo(LocalDate.now(), 200))
                            .withFr(null)
                    )
                );
        X509Certificate certificate = x509CertificateSupplier(emptyList()).get();
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(decoderWrapper.getCurrentCertificate()).willReturn(certificate);
        given(settingsRetriever.getSettingValue(RECOVERY_CERT_START_DAY_NOT_IT, SETTING_TYPE))
            .willReturn(0);
        given(settingsRetriever.getSettingValue(RECOVERY_CERT_END_DAY_NOT_IT, SETTING_TYPE))
            .willReturn(Integer.MAX_VALUE);

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, validationScanMode);

        // then
        assertEquals(expected, response);
    }

    @ParameterizedTest
    @MethodSource("createArguments_for_calculateValidity_when_normalRecovery_certificateOlderThan180days_countryNOT_IT")
    void calculateValidity_when_normalRecovery_certificateOlderThan180days_countryNOT_IT(
        ValidationScanMode validationScanMode, CertificateStatus expected) {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withDob(LocalDate.of(1900, 1, 1))
                .withNam(
                    new PersonName()
                        .withFn("Mario")
                        .withFnt("Mario")
                        .withGn("Rossi")
                        .withGnt("Rossi")
                )
                .withR(singletonList(
                        new RecoveryEntry()
                            .withCo("NOT_IT")
                            .withCi("fakeID")
                            .withIs("")
                            .withTg("")
                            .withDf(ChronoUnit.DAYS.addTo(LocalDate.now(), -200))
                            .withDu(ChronoUnit.DAYS.addTo(LocalDate.now(), 200))
                            .withFr(null)
                    )
                );
        X509Certificate certificate = x509CertificateSupplier(singletonList("ignored")).get();
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(decoderWrapper.getCurrentCertificate()).willReturn(certificate);
        given(settingsRetriever.getSettingValue(RECOVERY_CERT_START_DAY_NOT_IT, SETTING_TYPE))
            .willReturn(0);
        given(settingsRetriever.getSettingValue(RECOVERY_CERT_END_DAY_NOT_IT, SETTING_TYPE))
            .willReturn(Integer.MAX_VALUE);

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, validationScanMode);

        // then
        assertEquals(expected, response);
    }

    @ParameterizedTest
    @EnumSource(ValidationScanMode.class)
    void alwaysValid_when_DoubleRecovery_certificateNewerThan120days_countryIT(
        ValidationScanMode validationScanMode) {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withDob(LocalDate.of(1900, 1, 1))
                .withNam(
                    new PersonName()
                        .withFn("Mario")
                        .withFnt("Mario")
                        .withGn("Rossi")
                        .withGnt("Rossi")
                )
                .withR(singletonList(
                        new RecoveryEntry()
                            .withCo("IT")
                            .withCi("fakeID")
                            .withIs("")
                            .withTg("")
                            .withDf(ChronoUnit.DAYS.addTo(LocalDate.now(), -50))
                            .withDu(ChronoUnit.DAYS.addTo(LocalDate.now(), 200))
                            .withFr(null)
                    )
                );
        X509Certificate certificate = x509CertificateSupplier(singletonList(OID_RECOVERY)).get();
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(decoderWrapper.getCurrentCertificate()).willReturn(certificate);
        given(settingsRetriever.getSettingValue(RECOVERY_PV_CERT_START_DAY, SETTING_TYPE))
            .willReturn(0);
        given(settingsRetriever.getSettingValue(RECOVERY_PV_CERT_END_DAY, SETTING_TYPE))
            .willReturn(Integer.MAX_VALUE);

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, validationScanMode);

        // then
        assertEquals(VALID, response);
    }

    @ParameterizedTest
    @EnumSource(ValidationScanMode.class)
    void alwaysValid_when_DoubleRecovery_certificateOlderThan120days_countryIT(
        ValidationScanMode validationScanMode) {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withDob(LocalDate.of(1900, 1, 1))
                .withNam(
                    new PersonName()
                        .withFn("Mario")
                        .withFnt("Mario")
                        .withGn("Rossi")
                        .withGnt("Rossi")
                )
                .withR(singletonList(
                        new RecoveryEntry()
                            .withCo("IT")
                            .withCi("fakeID")
                            .withIs("")
                            .withTg("")
                            .withDf(ChronoUnit.DAYS.addTo(LocalDate.now(), -150))
                            .withDu(ChronoUnit.DAYS.addTo(LocalDate.now(), 200))
                            .withFr(null)
                    )
                );
        X509Certificate certificate =
            x509CertificateSupplier(singletonList(OID_ALT_RECOVERY)).get();
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(decoderWrapper.getCurrentCertificate()).willReturn(certificate);
        given(settingsRetriever.getSettingValue(RECOVERY_PV_CERT_START_DAY, SETTING_TYPE))
            .willReturn(0);
        given(settingsRetriever.getSettingValue(RECOVERY_PV_CERT_END_DAY, SETTING_TYPE))
            .willReturn(Integer.MAX_VALUE);

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, validationScanMode);

        // then
        assertEquals(VALID, response);
    }

    private static Stream<Arguments> createArguments_for_calculateValidity_when_normalRecovery() {
        return Stream.of(
            Arguments.of(BASE_DGP, VALID),
            Arguments.of(ENHANCED_DGP, VALID),
            Arguments.of(RSA_VISITORS_DGP, TEST_NEEDED),
            Arguments.of(WORK_DGP, VALID),
            Arguments.of(IT_ENTRY_DGP, VALID)
        );
    }

    private static Stream<Arguments> createArguments_for_calculateValidity_when_normalRecovery_certificateOlderThan180days_countryNOT_IT() {
        return Stream.of(
            Arguments.of(BASE_DGP, VALID),
            Arguments.of(ENHANCED_DGP, TEST_NEEDED),
            Arguments.of(RSA_VISITORS_DGP, TEST_NEEDED),
            Arguments.of(WORK_DGP, TEST_NEEDED),
            Arguments.of(IT_ENTRY_DGP, VALID)
        );
    }

    private Supplier<X509Certificate> x509CertificateSupplier(List<String> extendedKeyUsage) {
        return () -> new X509Certificate() {

            @Override
            public List<String> getExtendedKeyUsage() {
                return extendedKeyUsage;
            }

            @Override
            public boolean hasUnsupportedCriticalExtension() {
                return false;
            }

            @Override
            public Set<String> getCriticalExtensionOIDs() {
                return null;
            }

            @Override
            public Set<String> getNonCriticalExtensionOIDs() {
                return null;
            }

            @Override
            public byte[] getExtensionValue(String oid) {
                return new byte[0];
            }

            @Override
            public byte[] getEncoded() {
                return new byte[0];
            }

            @Override
            public void verify(PublicKey key) {
            }

            @Override
            public void verify(PublicKey key, String sigProvider) {
            }

            @Override
            public String toString() {
                return null;
            }

            @Override
            public PublicKey getPublicKey() {
                return null;
            }

            @Override
            public void checkValidity() {
            }

            @Override
            public void checkValidity(Date date) {
            }

            @Override
            public int getVersion() {
                return 0;
            }

            @Override
            public BigInteger getSerialNumber() {
                return null;
            }

            @Override
            public Principal getIssuerDN() {
                return null;
            }

            @Override
            public Principal getSubjectDN() {
                return null;
            }

            @Override
            public Date getNotBefore() {
                return null;
            }

            @Override
            public Date getNotAfter() {
                return null;
            }

            @Override
            public byte[] getTBSCertificate() {
                return new byte[0];
            }

            @Override
            public byte[] getSignature() {
                return new byte[0];
            }

            @Override
            public String getSigAlgName() {
                return null;
            }

            @Override
            public String getSigAlgOID() {
                return null;
            }

            @Override
            public byte[] getSigAlgParams() {
                return new byte[0];
            }

            @Override
            public boolean[] getIssuerUniqueID() {
                return new boolean[0];
            }

            @Override
            public boolean[] getSubjectUniqueID() {
                return new boolean[0];
            }

            @Override
            public boolean[] getKeyUsage() {
                return new boolean[0];
            }

            @Override
            public int getBasicConstraints() {
                return 0;
            }
        };
    }

}
