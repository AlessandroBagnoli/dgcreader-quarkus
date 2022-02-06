package unit.validationlogic;

import static com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID_YET;
import static com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus.VALID;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus;
import com.bagnoli.verificac19.dto.ValidationScanMode;
import com.bagnoli.verificac19.model.EnrichedDigitalCovidCertificate;
import com.bagnoli.verificac19.service.downloaders.SettingsRetriever;
import com.bagnoli.verificac19.service.validationlogic.ConcreteVaccineValidator;

import se.digg.dgc.payload.v1.PersonName;
import se.digg.dgc.payload.v1.VaccinationEntry;

@ExtendWith(MockitoExtension.class)
class ConcreteVaccineValidatorTest {

    private static final String VACCINE_START_DAY_NOT_COMPLETE = "vaccine_start_day_not_complete";
    private static final String VACCINE_END_DAY_NOT_COMPLETE = "vaccine_end_day_not_complete";
    private static final String VACCINE_START_DAY_COMPLETE = "vaccine_start_day_complete";
    private static final String VACCINE_END_DAY_COMPLETE = "vaccine_end_day_complete";
    private static final String JOHNSON = "EU/1/20/1525";

    private @Mock SettingsRetriever settingsRetriever;
    private @InjectMocks ConcreteVaccineValidator underTest;

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
                            .withCi("")
                            .withDn(1)
                            .withDt(LocalDate.of(2022, 2, 24))
                            .withIs("")
                            .withMa("")
                            .withMp("EU/1/20/1525")
                            .withSd(2)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE), any())).willReturn(
            15);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_NOT_COMPLETE),
            any())).willReturn(0);
        given(
            settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_NOT_COMPLETE), any())).willReturn(
            60);

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, ValidationScanMode.NORMAL_DGP);

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
                            .withCi("")
                            .withDn(1)
                            .withDt(LocalDate.of(2022, 1, 10))
                            .withIs("")
                            .withMa("")
                            .withMp("EU/1/20/1525")
                            .withSd(2)
                            .withTg("")
                            .withVp("")
                    )
                );
        given(settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_COMPLETE), any())).willReturn(
            15);
        given(settingsRetriever.getSettingValue(eq(VACCINE_START_DAY_NOT_COMPLETE),
            any())).willReturn(0);
        given(
            settingsRetriever.getSettingValue(eq(VACCINE_END_DAY_NOT_COMPLETE), any())).willReturn(
            60);

        // when
        CertificateStatus response =
            underTest.calculateValidity(dgc, ValidationScanMode.NORMAL_DGP);

        // then
        assertEquals(VALID, response);
    }
}
