package unit.validationlogic;

import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID;
import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.REVOKED;
import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.alessandrobagnoli.verificac19.dao.BlackListedPassDAO;
import com.github.alessandrobagnoli.verificac19.dao.RevokedPassDAO;
import com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus;
import com.github.alessandrobagnoli.verificac19.model.BlackListedPass;
import com.github.alessandrobagnoli.verificac19.model.RevokedPass;
import com.github.alessandrobagnoli.verificac19.service.validationlogic.ConcreteRevokedAndBlacklistedChecker;
import com.github.alessandrobagnoli.verificac19.utility.CertificateUtilities;

@ExtendWith(MockitoExtension.class)
class ConcreteRevokedAndBlacklistedCheckerTest {

    private @Mock RevokedPassDAO revokedPassDAO;
    private @Mock BlackListedPassDAO blackListedPassDAO;

    private @Captor ArgumentCaptor<String> captorIdentifier;

    private @InjectMocks ConcreteRevokedAndBlacklistedChecker underTest;

    @Test
    void test_revoked() {
        // given
        String identifier = "dummyIdentifier";
        given(revokedPassDAO.findByIdOptional(any())).willReturn(
            Optional.of(RevokedPass.builder().build()));

        // when
        Optional<CertificateStatus> result = underTest.check(identifier);

        // then
        then(revokedPassDAO).should().findByIdOptional(captorIdentifier.capture());
        String value = captorIdentifier.getValue();
        assertEquals(CertificateUtilities.sha256(identifier), value);
        then(blackListedPassDAO).shouldHaveNoInteractions();
        assertEquals(Optional.of(REVOKED), result);
    }

    @Test
    void test_notValid() {
        // given
        String identifier = "dummyIdentifier";
        given(revokedPassDAO.findByIdOptional(any())).willReturn(empty());
        given(blackListedPassDAO.findByIdOptional(identifier)).willReturn(Optional.of(
            BlackListedPass.builder().build()));

        // when
        Optional<CertificateStatus> result = underTest.check(identifier);

        // then
        then(revokedPassDAO).should().findByIdOptional(captorIdentifier.capture());
        String value = captorIdentifier.getValue();
        assertEquals(CertificateUtilities.sha256(identifier), value);
        then(blackListedPassDAO).should().findByIdOptional(identifier);
        assertEquals(Optional.of(NOT_VALID), result);
    }

    @Test
    void test_() {
        // given
        String identifier = "dummyIdentifier";
        given(revokedPassDAO.findByIdOptional(any())).willReturn(empty());
        given(blackListedPassDAO.findByIdOptional(identifier)).willReturn(empty());

        // when
        Optional<CertificateStatus> result = underTest.check(identifier);

        // then
        then(revokedPassDAO).should().findByIdOptional(captorIdentifier.capture());
        String value = captorIdentifier.getValue();
        assertEquals(CertificateUtilities.sha256(identifier), value);
        then(blackListedPassDAO).should().findByIdOptional(identifier);
        assertEquals(empty(), result);
    }

}
