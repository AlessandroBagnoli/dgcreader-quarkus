package unit.validationlogic;

import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID;
import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID_YET;
import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.TEST_NEEDED;
import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.VALID;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.BASE_DGP;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.IT_ENTRY_DGP;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.RSA_VISITORS_DGP;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.alessandrobagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate;
import com.github.alessandrobagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate.ExemptionEntry;
import com.github.alessandrobagnoli.verificac19.dto.GPValidResponse;
import com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode;
import com.github.alessandrobagnoli.verificac19.exception.EmptyDigitalCovidCertificateException;
import com.github.alessandrobagnoli.verificac19.service.validationlogic.ConcreteExemptionValidator;
import com.github.alessandrobagnoli.verificac19.service.validationlogic.RevokedAndBlacklistedChecker;

import se.digg.dgc.payload.v1.PersonName;

@ExtendWith(MockitoExtension.class)
class ConcreteExemptionValidatorTest {

    private @Mock RevokedAndBlacklistedChecker revokedAndBlacklistedChecker;
    private @InjectMocks ConcreteExemptionValidator underTest;

    @Test
    void throwsException_when_Empty() {
        // given
        EnrichedDigitalCovidCertificate dgc = new EnrichedDigitalCovidCertificate()
            .withE(emptyList());

        // when
        EmptyDigitalCovidCertificateException exception =
            assertThrows(EmptyDigitalCovidCertificateException.class,
                () -> underTest.calculateValidity(dgc, BASE_DGP));

        // then
        assertEquals("No exemptions found", exception.getMessage());
    }

    @Test
    void notValid_when_blacklisted() {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withE(singletonList(
                        ExemptionEntry.builder()
                            .co("")
                            .ci("fakeID")
                            .is("")
                            .tg("")
                            .df(null)
                            .du(null)
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
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(Optional.of(NOT_VALID));

        // when
        GPValidResponse.CertificateStatus response =
            underTest.calculateValidity(dgc, BASE_DGP);

        // then
        assertEquals(NOT_VALID, response);
    }

    @Test
    void notValidYet_when_validFromIsInFuture() {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withE(singletonList(
                        ExemptionEntry.builder()
                            .co("")
                            .ci("fakeID")
                            .is("")
                            .tg("")
                            .df(ChronoUnit.DAYS.addTo(LocalDate.now(), 50))
                            .du(ChronoUnit.DAYS.addTo(LocalDate.now(), 100))
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
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());

        // when
        GPValidResponse.CertificateStatus response =
            underTest.calculateValidity(dgc, BASE_DGP);

        // then
        assertEquals(NOT_VALID_YET, response);
    }

    @Test
    void notValid_when_validUntilIsPassed() {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withE(singletonList(
                        ExemptionEntry.builder()
                            .co("")
                            .ci("fakeID")
                            .is("")
                            .tg("")
                            .df(ChronoUnit.DAYS.addTo(LocalDate.now(), -50))
                            .du(ChronoUnit.DAYS.addTo(LocalDate.now(), -20))
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
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());

        // when
        GPValidResponse.CertificateStatus response =
            underTest.calculateValidity(dgc, BASE_DGP);

        // then
        assertEquals(NOT_VALID, response);
    }

    @Test
    void notValid_when_ITEntry() {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withE(singletonList(
                        ExemptionEntry.builder()
                            .co("")
                            .ci("fakeID")
                            .is("")
                            .tg("")
                            .df(ChronoUnit.DAYS.addTo(LocalDate.now(), -50))
                            .du(ChronoUnit.DAYS.addTo(LocalDate.now(), 100))
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
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());

        // when
        GPValidResponse.CertificateStatus response =
            underTest.calculateValidity(dgc, IT_ENTRY_DGP);

        // then
        assertEquals(NOT_VALID, response);
    }

    @Test
    void testNeeded_when_RSAVisitors() {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withE(singletonList(
                        ExemptionEntry.builder()
                            .co("")
                            .ci("fakeID")
                            .is("")
                            .tg("")
                            .df(ChronoUnit.DAYS.addTo(LocalDate.now(), -50))
                            .du(ChronoUnit.DAYS.addTo(LocalDate.now(), 100))
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
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());

        // when
        GPValidResponse.CertificateStatus response =
            underTest.calculateValidity(dgc, RSA_VISITORS_DGP);

        // then
        assertEquals(TEST_NEEDED, response);
    }

    @ParameterizedTest
    @EnumSource(value = ValidationScanMode.class,
        names = {"RSA_VISITORS_DGP", "IT_ENTRY_DGP"},
        mode = EnumSource.Mode.EXCLUDE)
    void valid_(ValidationScanMode scanMode) {
        // given
        EnrichedDigitalCovidCertificate dgc =
            (EnrichedDigitalCovidCertificate) new EnrichedDigitalCovidCertificate()
                .withE(singletonList(
                        ExemptionEntry.builder()
                            .co("")
                            .ci("fakeID")
                            .is("")
                            .tg("")
                            .df(ChronoUnit.DAYS.addTo(LocalDate.now(), -50))
                            .du(ChronoUnit.DAYS.addTo(LocalDate.now(), 100))
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
        given(revokedAndBlacklistedChecker.check("fakeID")).willReturn(empty());

        // when
        GPValidResponse.CertificateStatus response =
            underTest.calculateValidity(dgc, scanMode);

        // then
        assertEquals(VALID, response);
    }

}
