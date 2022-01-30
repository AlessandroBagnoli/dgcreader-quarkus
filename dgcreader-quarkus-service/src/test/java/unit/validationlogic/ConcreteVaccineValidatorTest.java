package unit.validationlogic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bagnoli.verificac19.service.downloaders.SettingsRetriever;
import com.bagnoli.verificac19.service.validationlogic.ConcreteVaccineValidator;

import se.digg.dgc.payload.v1.DigitalCovidCertificate;

@ExtendWith(MockitoExtension.class)
class ConcreteVaccineValidatorTest {

    private static final String VACCINE_START_DAY_NOT_COMPLETE = "vaccine_start_day_not_complete";
    private static final String VACCINE_END_DAY_NOT_COMPLETE = "vaccine_end_day_not_complete";
    private static final String VACCINE_START_DAY_COMPLETE = "vaccine_start_day_complete";
    private static final String VACCINE_END_DAY_COMPLETE = "vaccine_end_day_complete";
    private static final String JOHNSON = "EU/1/20/1525";

    @Mock SettingsRetriever settingsRetriever;

    @InjectMocks ConcreteVaccineValidator underTest;

    @Test
    void name() {
//        // given
//        DigitalCovidCertificate dgc = DigitalCovidCertificate.fromJsonString()
//        
//        // when
//        underTest.calculateValidity();
//        
//        // then
    }
}
