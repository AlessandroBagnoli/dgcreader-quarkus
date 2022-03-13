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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.alessandrobagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate;
import com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus;
import com.github.alessandrobagnoli.verificac19.exception.EmptyDigitalCovidCertificateException;
import com.github.alessandrobagnoli.verificac19.service.downloaders.SettingsRetriever;
import com.github.alessandrobagnoli.verificac19.service.validationlogic.ConcreteVaccineValidator;
import com.github.alessandrobagnoli.verificac19.service.validationlogic.RevokedAndBlacklistedChecker;

import se.digg.dgc.payload.v1.PersonName;
import se.digg.dgc.payload.v1.VaccinationEntry;

@ExtendWith(MockitoExtension.class)
class ConcreteVaccineValidatorTest {

    private static final String VACCINE_START_DAY_NOT_COMPLETE = "vaccine_start_day_not_complete";
    private static final String VACCINE_END_DAY_NOT_COMPLETE = "vaccine_end_day_not_complete";
    private static final String VACCINE_START_DAY_COMPLETE = "vaccine_start_day_complete";
    private static final String VACCINE_END_DAY_COMPLETE = "vaccine_end_day_complete";
    private static final String VACCINE_START_DAY_COMPLETE_IT = "vaccine_start_day_complete_IT";
    private static final String VACCINE_END_DAY_COMPLETE_IT = "vaccine_end_day_complete_IT";
    private static final String VACCINE_START_DAY_BOOSTER_IT = "vaccine_start_day_booster_IT";
    private static final String VACCINE_END_DAY_BOOSTER_IT = "vaccine_end_day_booster_IT";
    private static final String VACCINE_START_DAY_COMPLETE_NOT_IT =
        "vaccine_start_day_complete_NOT_IT";
    private static final String VACCINE_END_DAY_COMPLETE_NOT_IT = "vaccine_end_day_complete_NOT_IT";
    private static final String VACCINE_START_DAY_BOOSTER_NOT_IT =
        "vaccine_start_day_booster_NOT_IT";
    private static final String VACCINE_END_DAY_BOOSTER_NOT_IT = "vaccine_end_day_booster_NOT_IT";
    private static final String VACCINE_END_DAY_COMPLETE_EXTENDED_EMA =
        "vaccine_end_day_complete_extended_EMA";
    private static final String JOHNSON = "EU/1/20/1525";
    private static final String SPUTNIK = "Sputnik-V";
    private static final String PARTIAL = "PARTIAL";
    private static final String CYCLE = "CYCLE";
    private static final String BOOSTER = "BOOSTER";
    private static final String EMA_VACCINES = "EMA_vaccines";
    private static final String SETTING_TYPE = "GENERIC";
    private static final String COUNTRY_SAN_MARINO = "SM";

    private @Mock SettingsRetriever settingsRetriever;
    private @Mock RevokedAndBlacklistedChecker revokedAndBlacklistedChecker;
    private @InjectMocks ConcreteVaccineValidator underTest;

    @Test
    void throwsException_when_Empty() {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withV(emptyList());

        // when
        EmptyDigitalCovidCertificateException exception =
            assertThrows(EmptyDigitalCovidCertificateException.class,
                () -> underTest.calculateValidity(dgc, BASE_DGP));

        // then
        assertEquals("No vaccines found", exception.getMessage());
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("")
                            .withCi("fakeID")
                            .withDn(1)
                            .withDt(LocalDate.of(9999, 2, 24))
                            .withIs("")
                            .withMa("")
                            .withMp(JOHNSON)
                            .withSd(2)
                            .withTg("")
                            .withVp("")
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
    void notValid_when_vaccineNotPresentInSettingList() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("")
                            .withCi("fakeID")
                            .withDn(1)
                            .withDt(LocalDate.of(9999, 2, 24))
                            .withIs("")
                            .withMa("")
                            .withMp("vaccinationType")
                            .withSd(2)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(VACCINE_END_DAY_COMPLETE,
            "vaccinationType")).willReturn(null);

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, BASE_DGP);

        // then
        assertEquals(NOT_VALID, response);
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("")
                            .withCi("fakeID")
                            .withDn(1)
                            .withDt(LocalDate.of(1970, 1, 1))
                            .withIs("")
                            .withMa("")
                            .withMp("vaccinationType")
                            .withSd(2)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE), any())).willReturn(
            15);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_NOT_COMPLETE),
            any())).willReturn(0);
        given(
            settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_NOT_COMPLETE), any())).willReturn(
            60);

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, BASE_DGP);

        // then
        assertEquals(NOT_VALID, response);
    }

    @Test
    void notValidYet_when_dateInFuture() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("")
                            .withCi("fakeID")
                            .withDn(1)
                            .withDt(LocalDate.of(9999, 2, 24))
                            .withIs("")
                            .withMa("")
                            .withMp(JOHNSON)
                            .withSd(2)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE), any())).willReturn(
            15);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_NOT_COMPLETE),
            any())).willReturn(0);
        given(
            settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_NOT_COMPLETE), any())).willReturn(
            60);

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, BASE_DGP);

        // then
        assertEquals(NOT_VALID_YET, response);
    }

    @Test
    void valid_when_firstDoseOfJohnsonNormalVerification() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("")
                            .withCi("fakeID")
                            .withDn(1)
                            .withDt(LocalDate.of(2022, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp(JOHNSON)
                            .withSd(2)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE), any())).willReturn(
            15);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_NOT_COMPLETE),
            any())).willReturn(0);
        given(
            settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_NOT_COMPLETE), any())).willReturn(
            99999);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            JOHNSON);

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, BASE_DGP);

        // then
        assertEquals(VALID, response);
    }

    @Test
    void testNeeded_when_firstAndLastDoseOfJohnsonRSAVerification() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("IT")
                            .withCi("fakeID")
                            .withDn(1)
                            .withDt(LocalDate.of(2022, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp(JOHNSON)
                            .withSd(1)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE), any())).willReturn(
            250);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_COMPLETE),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_COMPLETE_IT),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            JOHNSON);

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, RSA_VISITORS_DGP);

        // then
        assertEquals(TEST_NEEDED, response);
    }

    @Test
    void valid_when_firstAndLastDoseOfJohnsonWorkVerification() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("IT")
                            .withCi("fakeID")
                            .withDn(1)
                            .withDt(LocalDate.of(2022, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp(JOHNSON)
                            .withSd(1)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE), any())).willReturn(
            250);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_COMPLETE),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_COMPLETE_IT),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            JOHNSON);

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, ENHANCED_DGP);

        // then
        assertEquals(VALID, response);
    }

    @Test
    void valid_when_firstAndLastDoseOfJohnsonEnhancedVerification() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("IT")
                            .withCi("fakeID")
                            .withDn(1)
                            .withDt(LocalDate.of(2022, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp(JOHNSON)
                            .withSd(1)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE), any())).willReturn(
            250);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_COMPLETE),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_COMPLETE_IT),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            JOHNSON);

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, ENHANCED_DGP);

        // then
        assertEquals(VALID, response);
    }

    @Test
    void notValid_when_PartialCycleRSAVisitorsVerificationNotEma() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("IT")
                            .withCi("fakeID")
                            .withDn(1)
                            .withDt(LocalDate.of(2022, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp("anyProduct")
                            .withSd(2)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE), any())).willReturn(
            250);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_NOT_COMPLETE),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_NOT_COMPLETE),
            any())).willReturn(3000);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(null);

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, RSA_VISITORS_DGP);

        // then
        assertEquals(NOT_VALID, response);
    }

    @Test
    void notValid_when_PartialCycleRSAVisitorsVerificationEma() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("IT")
                            .withCi("fakeID")
                            .withDn(1)
                            .withDt(LocalDate.of(2022, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp("anyProduct")
                            .withSd(2)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE), any())).willReturn(
            250);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_NOT_COMPLETE),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_NOT_COMPLETE),
            any())).willReturn(3000);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            "anyProduct;anotherProduct");

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, RSA_VISITORS_DGP);

        // then
        assertEquals(NOT_VALID, response);
    }

    @Test
    void testNeeded_when_CompletedCycleEnhancedVerificationEmaNotITDaysActiveOver180() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("NOT_IT")
                            .withCi("fakeID")
                            .withDn(2)
                            .withDt(LocalDate.of(2021, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp("anyProduct")
                            .withSd(2)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE), any())).willReturn(
            250);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_COMPLETE),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_COMPLETE_NOT_IT),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE_NOT_IT),
            any())).willReturn(3000);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            "anyProduct;anotherProduct");

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, ENHANCED_DGP);

        // then
        assertEquals(TEST_NEEDED, response);
    }

    @Test
    void valid_when_CompletedCycleEnhancedVerificationEmaNotITDaysActiveUnder180() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("NOT_IT")
                            .withCi("fakeID")
                            .withDn(2)
                            .withDt(ChronoUnit.DAYS.addTo(LocalDate.now(), -50))
                            .withIs("")
                            .withMa("")
                            .withMp("anyProduct")
                            .withSd(2)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE), any())).willReturn(
            250);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_COMPLETE),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_COMPLETE_NOT_IT),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE_NOT_IT),
            any())).willReturn(3000);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            "anyProduct;anotherProduct");

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, ENHANCED_DGP);

        // then
        assertEquals(VALID, response);
    }

    @Test
    void valid_when_CompletedCycleEnhancedVerificationEmaITDaysActiveUnder180() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("IT")
                            .withCi("fakeID")
                            .withDn(2)
                            .withDt(ChronoUnit.DAYS.addTo(LocalDate.now(), -50))
                            .withIs("")
                            .withMa("")
                            .withMp("anyProduct")
                            .withSd(2)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE), any())).willReturn(
            250);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_COMPLETE),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_COMPLETE_IT),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE_IT),
            any())).willReturn(3000);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            "anyProduct;anotherProduct");

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, ENHANCED_DGP);

        // then
        assertEquals(VALID, response);
    }

    @Test
    void valid_when_CompletedCycleEnhancedVerificationEmaITDaysActiveOver180() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("IT")
                            .withCi("fakeID")
                            .withDn(2)
                            .withDt(ChronoUnit.DAYS.addTo(LocalDate.now(), -200))
                            .withIs("")
                            .withMa("")
                            .withMp("anyProduct")
                            .withSd(2)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE), any())).willReturn(
            250);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_COMPLETE),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_COMPLETE_IT),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE_IT),
            any())).willReturn(3000);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            "anyProduct;anotherProduct");

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, ENHANCED_DGP);

        // then
        assertEquals(VALID, response);
    }

    @Test
    void valid_when_PartialCycleEnhancedVerificationEma() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("IT")
                            .withCi("fakeID")
                            .withDn(1)
                            .withDt(LocalDate.of(2022, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp("anyProduct")
                            .withSd(2)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE), any())).willReturn(
            250);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_NOT_COMPLETE),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_NOT_COMPLETE),
            any())).willReturn(3000);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            "anyProduct;anotherProduct");

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, ENHANCED_DGP);

        // then
        assertEquals(VALID, response);
    }

    @Test
    void notValid_when_PartialCycleWorkVerificationNotEma() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("IT")
                            .withCi("fakeID")
                            .withDn(1)
                            .withDt(LocalDate.of(2022, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp("anyProduct")
                            .withSd(2)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE), any())).willReturn(
            250);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_NOT_COMPLETE),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_NOT_COMPLETE),
            any())).willReturn(3000);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            "notInList");

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, WORK_DGP);

        // then
        assertEquals(NOT_VALID, response);
    }

    @Test
    void notValid_when_PartialCycleEnhancedVerificationNotEma() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("IT")
                            .withCi("fakeID")
                            .withDn(1)
                            .withDt(LocalDate.of(2022, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp("anyProduct")
                            .withSd(2)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE), any())).willReturn(
            250);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_NOT_COMPLETE),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_NOT_COMPLETE),
            any())).willReturn(3000);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            "notInList");

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, ENHANCED_DGP);

        // then
        assertEquals(NOT_VALID, response);
    }

    @Test
    void testNeeded_whenBoosterCycleWorkVerificationNotEma() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("IT")
                            .withCi("fakeID")
                            .withDn(3)
                            .withDt(LocalDate.of(2022, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp("anyProduct")
                            .withSd(2)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE), any())).willReturn(
            250);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_BOOSTER_IT),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_BOOSTER_IT),
            any())).willReturn(3000);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            "notInList");

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, WORK_DGP);

        // then
        assertEquals(TEST_NEEDED, response);
    }

    @Test
    void testNeeded_whenCompleteCycleWorkVerificationEmaNotITOver50() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("NOT_IT")
                            .withCi("fakeID")
                            .withDn(2)
                            .withDt(LocalDate.of(2021, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp("anyProduct")
                            .withSd(2)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE), any())).willReturn(
            250);
        given(
            settingsRetriever.getSettingValue(VACCINE_START_DAY_COMPLETE, "anyProduct")).willReturn(
            0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_COMPLETE_NOT_IT),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE_NOT_IT),
            any())).willReturn(3000);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            "anyProduct");

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, WORK_DGP);

        // then
        assertEquals(TEST_NEEDED, response);
    }

    @Test
    void valid_whenCompleteCycleWorkVerificationEmaNotITUnder50() {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withDob(LocalDate.of(1993, 1, 1))
                .withNam(
                    new PersonName()
                        .withFn("Mario")
                        .withFnt("Mario")
                        .withGn("Rossi")
                        .withGnt("Rossi")
                )
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("NOT_IT")
                            .withCi("fakeID")
                            .withDn(2)
                            .withDt(LocalDate.of(2021, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp("anyProduct")
                            .withSd(2)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE), any())).willReturn(
            250);
        given(
            settingsRetriever.getSettingValue(VACCINE_START_DAY_COMPLETE, "anyProduct")).willReturn(
            0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_COMPLETE_NOT_IT),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE_NOT_IT),
            any())).willReturn(3000);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            "anyProduct");

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, WORK_DGP);

        // then
        assertEquals(VALID, response);
    }

    @Test
    void notValid_whenPartialCycleITEntryVerificationEmaNotIT() {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withDob(LocalDate.of(1993, 1, 1))
                .withNam(
                    new PersonName()
                        .withFn("Mario")
                        .withFnt("Mario")
                        .withGn("Rossi")
                        .withGnt("Rossi")
                )
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("NOT_IT")
                            .withCi("fakeID")
                            .withDn(1)
                            .withDt(LocalDate.of(2021, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp("anyProduct")
                            .withSd(2)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE), any())).willReturn(
            250);
        given(
            settingsRetriever.getSettingValue(VACCINE_START_DAY_NOT_COMPLETE,
                "anyProduct")).willReturn(
            0);
        given(
            settingsRetriever.getSettingValue(VACCINE_END_DAY_NOT_COMPLETE,
                "anyProduct")).willReturn(
            3000);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            "anyProduct");

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, IT_ENTRY_DGP);

        // then
        assertEquals(NOT_VALID, response);
    }

    @Test
    void valid_whenBoosterRSAVerificationEmaNotIT() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("NOT_IT")
                            .withCi("fakeID")
                            .withDn(3)
                            .withDt(LocalDate.of(2021, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp("anyProduct")
                            .withSd(3)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(VACCINE_END_DAY_COMPLETE, "anyProduct")).willReturn(
            250);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_BOOSTER_NOT_IT),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_BOOSTER_NOT_IT),
            any())).willReturn(3000);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            "anyProduct");

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, RSA_VISITORS_DGP);

        // then
        assertEquals(VALID, response);
    }

    @Test
    void valid_whenBoosterWorkVerificationEmaPersonAgeOver50() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("NOT_IT")
                            .withCi("fakeID")
                            .withDn(3)
                            .withDt(LocalDate.of(2021, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp("anyProduct")
                            .withSd(3)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(VACCINE_END_DAY_COMPLETE, "anyProduct")).willReturn(
            250);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_BOOSTER_NOT_IT),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_BOOSTER_NOT_IT),
            any())).willReturn(3000);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            "anyProduct");

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, WORK_DGP);

        // then
        assertEquals(VALID, response);
    }

    @Test
    void valid_whenBoosterItEntryVerificationEma() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("IT")
                            .withCi("fakeID")
                            .withDn(3)
                            .withDt(LocalDate.of(2021, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp("anyProduct")
                            .withSd(3)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(VACCINE_END_DAY_COMPLETE, "anyProduct")).willReturn(
            250);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_BOOSTER_IT),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_BOOSTER_IT),
            any())).willReturn(3000);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            "anyProduct");

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, IT_ENTRY_DGP);

        // then
        assertEquals(VALID, response);
    }

    @Test
    void notValid_whenPartialCycleItEntryVerificationEma() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("IT")
                            .withCi("fakeID")
                            .withDn(1)
                            .withDt(LocalDate.of(2021, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp("anyProduct")
                            .withSd(2)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(VACCINE_END_DAY_COMPLETE, "anyProduct")).willReturn(
            250);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_NOT_COMPLETE),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_NOT_COMPLETE),
            any())).willReturn(3000);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            "anyProduct");

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, IT_ENTRY_DGP);

        // then
        assertEquals(NOT_VALID, response);
    }

    @Test
    void testNeeded_whenBoosterRSAVerificationNotEMA() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("NOT_IT")
                            .withCi("fakeID")
                            .withDn(3)
                            .withDt(LocalDate.of(2021, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp("anyProduct")
                            .withSd(3)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(VACCINE_END_DAY_COMPLETE, "anyProduct")).willReturn(
            250);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_BOOSTER_NOT_IT),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_BOOSTER_NOT_IT),
            any())).willReturn(3000);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            "notInList");

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, RSA_VISITORS_DGP);

        // then
        assertEquals(TEST_NEEDED, response);
    }

    @Test
    void testNeeded_whenBoosterEnhancedVerificationNotEMA() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("NOT_IT")
                            .withCi("fakeID")
                            .withDn(3)
                            .withDt(LocalDate.of(2021, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp("anyProduct")
                            .withSd(3)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(VACCINE_END_DAY_COMPLETE, "anyProduct")).willReturn(
            250);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_BOOSTER_NOT_IT),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_BOOSTER_NOT_IT),
            any())).willReturn(3000);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            "notInList");

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, ENHANCED_DGP);

        // then
        assertEquals(TEST_NEEDED, response);
    }

    @Test
    void notValid_whenBoosterITEntryVerificationNotEMA() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("NOT_IT")
                            .withCi("fakeID")
                            .withDn(3)
                            .withDt(LocalDate.of(2021, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp("anyProduct")
                            .withSd(3)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(VACCINE_END_DAY_COMPLETE, "anyProduct")).willReturn(
            250);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_BOOSTER_NOT_IT),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_BOOSTER_NOT_IT),
            any())).willReturn(3000);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            "notInList");

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, IT_ENTRY_DGP);

        // then
        assertEquals(NOT_VALID, response);
    }

    @Test
    void notValid_whenBoosterBaseVerificationNotEMA() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("NOT_IT")
                            .withCi("fakeID")
                            .withDn(3)
                            .withDt(LocalDate.of(2021, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp("anyProduct")
                            .withSd(3)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(VACCINE_END_DAY_COMPLETE, "anyProduct")).willReturn(
            250);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_BOOSTER_NOT_IT),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_BOOSTER_NOT_IT),
            any())).willReturn(3000);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            "notInList");

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, BASE_DGP);

        // then
        assertEquals(NOT_VALID, response);
    }

    @Test
    void valid_whenBoosterJohnsonBaseVerification() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("IT")
                            .withCi("fakeID")
                            .withDn(2)
                            .withDt(LocalDate.of(2021, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp(JOHNSON)
                            .withSd(2)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(VACCINE_END_DAY_COMPLETE, JOHNSON)).willReturn(
            250);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_BOOSTER_IT),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_BOOSTER_IT),
            any())).willReturn(3000);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            JOHNSON);

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, BASE_DGP);

        // then
        assertEquals(VALID, response);
    }

    @Test
    void valid_whenSputnikSanMarinoBaseVerification() {
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
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo(COUNTRY_SAN_MARINO)
                            .withCi("fakeID")
                            .withDn(2)
                            .withDt(LocalDate.of(2021, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp(SPUTNIK)
                            .withSd(2)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(VACCINE_END_DAY_COMPLETE, SPUTNIK)).willReturn(
            250);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_COMPLETE),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_COMPLETE_NOT_IT),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE_NOT_IT),
            any())).willReturn(3000);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            "notInList");

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, BASE_DGP);

        // then
        assertEquals(VALID, response);
    }

    @Test
    void notValid_whenSputnikNotFromSanMarinoWorkVerificationAgeUnder50() {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withDob(ChronoUnit.YEARS.addTo(LocalDate.now(), -35))
                .withNam(
                    new PersonName()
                        .withFn("Mario")
                        .withFnt("Mario")
                        .withGn("Rossi")
                        .withGnt("Rossi")
                )
                .withV(singletonList(
                        new VaccinationEntry()
                            .withCo("IT")
                            .withCi("fakeID")
                            .withDn(2)
                            .withDt(LocalDate.of(2021, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp(SPUTNIK)
                            .withSd(2)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());
        given(settingsRetriever.getSettingValue(VACCINE_END_DAY_COMPLETE, SPUTNIK)).willReturn(
            250);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_COMPLETE),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_COMPLETE_IT),
            any())).willReturn(0);
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE_IT),
            any())).willReturn(3000);
        given(settingsRetriever.getSettingValueAsString(eq(EMA_VACCINES), any())).willReturn(
            "notInList");

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, WORK_DGP);

        // then
        assertEquals(NOT_VALID, response);
    }

}
