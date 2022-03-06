package unit.validationlogic;

import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID;
import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID_YET;
import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.VALID;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.BASE_DGP;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.IT_ENTRY_DGP;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.WORK_DGP;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
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
import com.github.alessandrobagnoli.verificac19.service.downloaders.SettingsRetriever;
import com.github.alessandrobagnoli.verificac19.service.validationlogic.ConcreteTestValidator;
import com.github.alessandrobagnoli.verificac19.service.validationlogic.RevokedAndBlacklistedChecker;

import se.digg.dgc.payload.v1.PersonName;
import se.digg.dgc.payload.v1.TestEntry;

@ExtendWith(MockitoExtension.class)
class ConcreteTestValidatorTest {

    private static final String RAPID_TEST_START_HOUR = "rapid_test_start_hours";
    private static final String RAPID_TEST_END_HOUR = "rapid_test_end_hours";
    private static final String MOLECULAR_TEST_START_HOUR = "molecular_test_start_hours";
    private static final String MOLECULAR_TEST_END_HOUR = "molecular_test_end_hours";
    private static final String DETECTED = "260373001";
    private static final String TYPE_RAPID = "LP217198-3";
    private static final String TYPE_MOLECULAR = "LP6464-4";
    private static final String SETTING_TYPE = "GENERIC";

    private @Mock SettingsRetriever settingsRetriever;
    private @Mock RevokedAndBlacklistedChecker revokedAndBlacklistedChecker;

    private @InjectMocks ConcreteTestValidator underTest;

    @Test
    void throwsException_when_Empty() {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withDob(LocalDate.now())
                .withT(emptyList());

        // when
        EmptyDigitalCovidCertificateException exception =
            assertThrows(EmptyDigitalCovidCertificateException.class,
                () -> underTest.calculateValidity(dgc, BASE_DGP));

        // then
        assertEquals("No tests found", exception.getMessage());
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
                .withT(singletonList(
                        new TestEntry()
                            .withCo("")
                            .withCi("fakeID")
                            .withIs("")
                            .withTg("")
                            .withTt(TYPE_RAPID)
                            .withTr("testResult")
                            .withTc("")
                            .withSc(ChronoUnit.HOURS.addTo(Instant.now(), -1))
                            .withNm("")
                            .withMa("")
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
    void notValid_when_covidDetected() {
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
                .withT(singletonList(
                        new TestEntry()
                            .withCo("")
                            .withCi("fakeID")
                            .withIs("")
                            .withTg("")
                            .withTt(TYPE_RAPID)
                            .withTr(DETECTED)
                            .withTc("")
                            .withSc(ChronoUnit.HOURS.addTo(Instant.now(), -1))
                            .withNm("")
                            .withMa("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, BASE_DGP);

        // then
        assertEquals(NOT_VALID, response);
    }

    @Test
    void notValid_when_testTypeUnknown() {
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
                .withT(singletonList(
                        new TestEntry()
                            .withCo("")
                            .withCi("fakeID")
                            .withIs("")
                            .withTg("")
                            .withTt("unknownType")
                            .withTr("")
                            .withTc("")
                            .withSc(ChronoUnit.HOURS.addTo(Instant.now(), -1))
                            .withNm("")
                            .withMa("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());

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
                .withT(singletonList(
                        new TestEntry()
                            .withCo("")
                            .withCi("fakeID")
                            .withIs("")
                            .withTg("")
                            .withTt(TYPE_RAPID)
                            .withTr("")
                            .withTc("")
                            .withSc(ChronoUnit.HOURS.addTo(Instant.now(), 1))
                            .withNm("")
                            .withMa("")
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
    void notValid_when_expired() {
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
                .withT(singletonList(
                        new TestEntry()
                            .withCo("")
                            .withCi("fakeID")
                            .withIs("")
                            .withTg("")
                            .withTt(TYPE_RAPID)
                            .withTr("")
                            .withTc("")
                            .withSc(ChronoUnit.HOURS.addTo(Instant.now(), -50))
                            .withNm("")
                            .withMa("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(RAPID_TEST_START_HOUR, SETTING_TYPE))
            .willReturn(0);
        given(settingsRetriever.getSettingValue(RAPID_TEST_END_HOUR, SETTING_TYPE))
            .willReturn(48);

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, BASE_DGP);

        // then
        assertEquals(NOT_VALID, response);
    }

    @ParameterizedTest
    @EnumSource(value = ValidationScanMode.class,
        mode = EnumSource.Mode.INCLUDE,
        names = {"ENHANCED_DGP", "RSA_VISITORS_DGP"})
    void calculateValidity_when_enhancedOrRsaVisitorsVerification(
        ValidationScanMode validationScanMode) {
        // given
        EnrichedDigitalCovidCertificate dgc = new EnrichedDigitalCovidCertificate();

        // when
        CertificateStatus result = underTest.calculateValidity(dgc, validationScanMode);

        // then
        assertEquals(NOT_VALID, result);
    }

    @ParameterizedTest
    @MethodSource("createArguments_for_calculateValidity_when_AgeUnder50")
    void calculateValidity_when_AgeUnder50(ValidationScanMode validationScanMode,
        CertificateStatus expected) {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withDob(ChronoUnit.YEARS.addTo(LocalDate.now(), -30))
                .withNam(
                    new PersonName()
                        .withFn("Mario")
                        .withFnt("Mario")
                        .withGn("Rossi")
                        .withGnt("Rossi")
                )
                .withT(singletonList(
                        new TestEntry()
                            .withCo("")
                            .withCi("fakeID")
                            .withIs("")
                            .withTg("")
                            .withTt(TYPE_RAPID)
                            .withTr("testResult")
                            .withTc("")
                            .withSc(ChronoUnit.HOURS.addTo(Instant.now(), -1))
                            .withNm("")
                            .withMa("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(RAPID_TEST_START_HOUR, SETTING_TYPE))
            .willReturn(0);
        given(settingsRetriever.getSettingValue(RAPID_TEST_END_HOUR, SETTING_TYPE))
            .willReturn(Integer.MAX_VALUE);

        // when
        CertificateStatus result = underTest.calculateValidity(dgc, validationScanMode);

        // then
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @MethodSource("createArguments_for_calculateValidity_when_AgeOver50")
    void calculateValidity_when_AgeOver50(ValidationScanMode validationScanMode,
        CertificateStatus expected) {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withDob(ChronoUnit.YEARS.addTo(LocalDate.now(), -60))
                .withNam(
                    new PersonName()
                        .withFn("Mario")
                        .withFnt("Mario")
                        .withGn("Rossi")
                        .withGnt("Rossi")
                )
                .withT(singletonList(
                        new TestEntry()
                            .withCo("")
                            .withCi("fakeID")
                            .withIs("")
                            .withTg("")
                            .withTt(TYPE_MOLECULAR)
                            .withTr("testResult")
                            .withTc("")
                            .withSc(ChronoUnit.HOURS.addTo(Instant.now(), -1))
                            .withNm("")
                            .withMa("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(MOLECULAR_TEST_START_HOUR, SETTING_TYPE))
            .willReturn(0);
        given(settingsRetriever.getSettingValue(MOLECULAR_TEST_END_HOUR, SETTING_TYPE))
            .willReturn(Integer.MAX_VALUE);

        // when
        CertificateStatus result = underTest.calculateValidity(dgc, validationScanMode);

        // then
        assertEquals(expected, result);
    }

    @Test
    void calculateValidity_when_AgeOver50_workValidation() {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withDob(ChronoUnit.YEARS.addTo(LocalDate.now(), -60));

        // when
        CertificateStatus result = underTest.calculateValidity(dgc, WORK_DGP);

        // then
        assertEquals(NOT_VALID, result);
    }

    private static Stream<Arguments> createArguments_for_calculateValidity_when_AgeUnder50() {
        return Stream.of(
            Arguments.of(BASE_DGP, VALID),
            Arguments.of(WORK_DGP, VALID),
            Arguments.of(IT_ENTRY_DGP, VALID)
        );
    }

    private static Stream<Arguments> createArguments_for_calculateValidity_when_AgeOver50() {
        return Stream.of(
            Arguments.of(BASE_DGP, VALID),
            Arguments.of(IT_ENTRY_DGP, VALID)
        );
    }

}
