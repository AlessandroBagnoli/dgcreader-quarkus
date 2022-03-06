package unit.service;

import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.BASE_DGP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.alessandrobagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate;
import com.github.alessandrobagnoli.verificac19.dto.GPValidResponse;
import com.github.alessandrobagnoli.verificac19.exception.ServiceException;
import com.github.alessandrobagnoli.verificac19.service.ConcreteGreenPassService;
import com.github.alessandrobagnoli.verificac19.service.GDCDecoderWrapper;
import com.github.alessandrobagnoli.verificac19.service.LockWhileDrlSync;
import com.github.alessandrobagnoli.verificac19.service.validationlogic.Validator;

@ExtendWith(MockitoExtension.class)
class ConcreteGreenPassServiceTest {

    private @Mock LockWhileDrlSync lockWhileDrlSync;
    private @Mock GDCDecoderWrapper gdcDecoderWrapper;
    private @Mock Validator validator;

    private @InjectMocks ConcreteGreenPassService underTest;

    @Test
    void exception_whenDRLUpdating() {
        // given
        String base45 = "dummyDgc";
        given(lockWhileDrlSync.isLocked()).willReturn(true);

        // when
        ServiceException actual =
            assertThrows(ServiceException.class, () -> underTest.validate(base45, BASE_DGP));

        // then
        assertEquals("DRL is updating", actual.getMessage());
        then(lockWhileDrlSync).shouldHaveNoMoreInteractions();
        then(gdcDecoderWrapper).shouldHaveNoInteractions();
        then(validator).shouldHaveNoInteractions();
    }

    @Test
    void validate_string() {
        // given
        String base45 = "dummyDgc";
        EnrichedDigitalCovidCertificate dgc = new EnrichedDigitalCovidCertificate();
        GPValidResponse validated = GPValidResponse.builder().build();
        given(lockWhileDrlSync.isLocked()).willReturn(false);
        given(gdcDecoderWrapper.decode(base45)).willReturn(dgc);
        given(validator.validate(dgc, BASE_DGP)).willReturn(validated);

        // when
        GPValidResponse actual = underTest.validate(base45, BASE_DGP);

        // then
        assertEquals(validated, actual);
        then(lockWhileDrlSync).shouldHaveNoMoreInteractions();
        then(gdcDecoderWrapper).shouldHaveNoMoreInteractions();
        then(validator).shouldHaveNoMoreInteractions();
    }

    @Test
    void validate_file() {
        // given
        byte[] file = new byte[] {1, 2, 3};
        EnrichedDigitalCovidCertificate dgc = new EnrichedDigitalCovidCertificate();
        GPValidResponse validated = GPValidResponse.builder().build();
        given(lockWhileDrlSync.isLocked()).willReturn(false);
        given(gdcDecoderWrapper.decode(file)).willReturn(dgc);
        given(validator.validate(dgc, BASE_DGP)).willReturn(validated);

        // when
        GPValidResponse actual = underTest.validate(file, BASE_DGP);

        // then
        assertEquals(validated, actual);
        then(lockWhileDrlSync).shouldHaveNoMoreInteractions();
        then(gdcDecoderWrapper).shouldHaveNoMoreInteractions();
        then(validator).shouldHaveNoMoreInteractions();
    }

}
