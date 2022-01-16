package com.bagnoli.verificac19.service.validationlogic;

import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.enterprise.context.ApplicationScoped;

import com.bagnoli.verificac19.dto.GPValidResponse;
import com.bagnoli.verificac19.dto.GPValidResponse.PersonData;
import com.bagnoli.verificac19.service.downloaders.SettingsRetriever;

import lombok.RequiredArgsConstructor;
import se.digg.dgc.payload.v1.DigitalCovidCertificate;
import se.digg.dgc.payload.v1.TestEntry;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteTestValidator implements TestValidator {

    private static final String RAPID_TEST_START_HOUR = "rapid_test_start_hours";
    private static final String RAPID_TEST_END_HOUR = "rapid_test_end_hours";
    private static final String MOLECULAR_TEST_START_HOUR = "molecular_test_start_hours";
    private static final String MOLECULAR_TEST_END_HOUR = "molecular_test_end_hours";
    private static final String DETECTED = "260373001";
    // https://ec.europa.eu/health/sites/default/files/ehealth/docs/digital-green-certificates_dt-specifications_en.pdf
    private static final String TYPE_RAPID = "LP217198-3";    // RAT, Rapid Antigen Test
    private static final String TYPE_MOLECULAR = "LP6464-4";
    // NAAT, Nucleic Acid Amplification Test
    private static final String SETTING_TYPE = "GENERIC";
    
    private final SettingsRetriever settingsRetriever;

    @Override
    public GPValidResponse calculateValidity(DigitalCovidCertificate digitalCovidCertificate) {
        TestEntry testEntry = digitalCovidCertificate.getT().stream().findFirst().orElseThrow();
        PersonData personData = PersonData.builder()
            .name(digitalCovidCertificate.getNam().getGn())
            .surname(digitalCovidCertificate.getNam().getFn())
            .birthDate(digitalCovidCertificate.getDateOfBirth().asLocalDate())
            .build();
        String testType = testEntry.getTt();
        String testResult = testEntry.getTr();
        LocalDateTime dateTimeOfSampleCollection =
            LocalDateTime.ofInstant(testEntry.getSc(), ZoneId.of("Europe/Rome"));
        LocalDateTime now = LocalDateTime.now();

        if (DETECTED.equals(testResult)) {
            return GPValidResponse.builder()
                .valid(false)
                .errorDescription("Test result is detected")
                .personData(personData)
                .build();
        }

        LocalDateTime startDateTime;
        LocalDateTime endDateTime;

        if (TYPE_RAPID.equals(testType)) {
            startDateTime = dateTimeOfSampleCollection.plusHours(
                settingsRetriever.getSettingValue(RAPID_TEST_START_HOUR, SETTING_TYPE));
            endDateTime = dateTimeOfSampleCollection.plusHours(
                settingsRetriever.getSettingValue(RAPID_TEST_END_HOUR, SETTING_TYPE));
        } else if (TYPE_MOLECULAR.equals(testType)) {
            startDateTime = dateTimeOfSampleCollection.plusHours(
                settingsRetriever.getSettingValue(MOLECULAR_TEST_START_HOUR, SETTING_TYPE));
            endDateTime = dateTimeOfSampleCollection.plusHours(
                settingsRetriever.getSettingValue(MOLECULAR_TEST_END_HOUR, SETTING_TYPE));
        } else {
            return GPValidResponse.builder()
                .valid(false)
                .errorDescription("Unknown Test type: " + testType)
                .personData(personData)
                .build();
        }

        if (startDateTime.isAfter(now)) {
            return GPValidResponse.builder()
                .valid(false)
                .errorDescription("Not valid yet")
                .personData(personData)
                .build();
        }

        if (now.isAfter(endDateTime)) {
            return GPValidResponse.builder()
                .valid(false)
                .errorDescription("Expired")
                .personData(personData)
                .build();
        }

        return GPValidResponse.builder()
            .valid(true)
            .personData(personData)
            .validUntil(endDateTime.toLocalDate())
            .build();

    }

}
