package unit.validationlogic;

import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_EU_DCC;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.BASE_DGP;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.then;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.alessandrobagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate;
import com.github.alessandrobagnoli.verificac19.dto.GPValidResponse;
import com.github.alessandrobagnoli.verificac19.service.validationlogic.ConcreteValidator;
import com.github.alessandrobagnoli.verificac19.service.validationlogic.ExemptionValidator;
import com.github.alessandrobagnoli.verificac19.service.validationlogic.RecoveryValidator;
import com.github.alessandrobagnoli.verificac19.service.validationlogic.TestValidator;
import com.github.alessandrobagnoli.verificac19.service.validationlogic.VaccineValidator;

import se.digg.dgc.payload.v1.PersonName;
import se.digg.dgc.payload.v1.RecoveryEntry;
import se.digg.dgc.payload.v1.TestEntry;
import se.digg.dgc.payload.v1.VaccinationEntry;

@ExtendWith(MockitoExtension.class)
class ConcreteValidatorTest {

    private @Mock VaccineValidator vaccineValidator;
    private @Mock TestValidator testValidator;
    private @Mock RecoveryValidator recoveryValidator;
    private @Mock ExemptionValidator exemptionValidator;

    private @InjectMocks ConcreteValidator underTest;

    @Test
    void validate_whenVaccination() {
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
                    )
                );

        // when
        underTest.validate(dgc, BASE_DGP);

        // then
        then(vaccineValidator).should().calculateValidity(dgc, BASE_DGP);
        then(testValidator).shouldHaveNoInteractions();
        then(recoveryValidator).shouldHaveNoInteractions();
        then(exemptionValidator).shouldHaveNoInteractions();
    }

    @Test
    void validate_whenTest() {
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
                    )
                );

        // when
        underTest.validate(dgc, BASE_DGP);

        // then
        then(testValidator).should().calculateValidity(dgc, BASE_DGP);
        then(vaccineValidator).shouldHaveNoInteractions();
        then(recoveryValidator).shouldHaveNoInteractions();
        then(exemptionValidator).shouldHaveNoInteractions();
    }

    @Test
    void validate_whenRecovery() {
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
                    )
                );

        // when
        underTest.validate(dgc, BASE_DGP);

        // then
        then(recoveryValidator).should().calculateValidity(dgc, BASE_DGP);
        then(vaccineValidator).shouldHaveNoInteractions();
        then(testValidator).shouldHaveNoInteractions();
        then(exemptionValidator).shouldHaveNoInteractions();
    }

    @Test
    void validate_whenExemption() {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withE(singletonList(
                        EnrichedDigitalCovidCertificate.ExemptionEntry.builder()
                            .build()
                    )
                )
                .withDob(LocalDate.of(1900, 1, 1))
                .withNam(
                    new PersonName()
                        .withFn("Mario")
                        .withFnt("Mario")
                        .withGn("Rossi")
                        .withGnt("Rossi")
                );

        // when
        underTest.validate(dgc, BASE_DGP);

        // then
        then(exemptionValidator).should().calculateValidity(dgc, BASE_DGP);
        then(testValidator).shouldHaveNoInteractions();
        then(recoveryValidator).shouldHaveNoInteractions();
        then(vaccineValidator).shouldHaveNoInteractions();
    }

    @Test
    void validate_whenEmpty() {
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
                );

        // when
        GPValidResponse validate = underTest.validate(dgc, BASE_DGP);

        // then
        then(exemptionValidator).shouldHaveNoInteractions();
        then(testValidator).shouldHaveNoInteractions();
        then(recoveryValidator).shouldHaveNoInteractions();
        then(vaccineValidator).shouldHaveNoInteractions();

        assertEquals(NOT_EU_DCC, validate.getCertificateStatus());
    }

}
