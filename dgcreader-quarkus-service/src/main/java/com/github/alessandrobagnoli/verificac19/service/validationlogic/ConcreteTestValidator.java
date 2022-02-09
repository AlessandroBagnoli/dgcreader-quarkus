package com.github.alessandrobagnoli.verificac19.service.validationlogic;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import com.github.alessandrobagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate;
import com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode;
import com.github.alessandrobagnoli.verificac19.exception.EmptyDigitalCovidCertificateException;
import com.github.alessandrobagnoli.verificac19.service.downloaders.SettingsRetriever;
import com.github.alessandrobagnoli.verificac19.dto.GPValidResponse;

import lombok.RequiredArgsConstructor;
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
    private final RevokedAndBlacklistedChecker revokedAndBlacklistedChecker;

    @Override
    public GPValidResponse.CertificateStatus calculateValidity(
        EnrichedDigitalCovidCertificate digitalCovidCertificate,
        ValidationScanMode validationScanMode) {
        TestEntry testEntry = digitalCovidCertificate.getT().stream()
            .reduce((first, second) -> second)
            .orElseThrow(() -> new EmptyDigitalCovidCertificateException("No tests found"));
        String testType = testEntry.getTt();
        String testResult = testEntry.getTr();
        LocalDateTime dateTimeOfSampleCollection =
            LocalDateTime.ofInstant(testEntry.getSc(), ZoneId.of("Europe/Rome"));
        LocalDateTime now = LocalDateTime.now();
        String certificateIdentifier = testEntry.getCi();

        Optional<GPValidResponse.CertificateStatus> check =
            revokedAndBlacklistedChecker.check(certificateIdentifier);
        if (check.isPresent()) {
            return check.get();
        }

        if (DETECTED.equals(testResult)) {
            return GPValidResponse.CertificateStatus.NOT_VALID;
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
            return GPValidResponse.CertificateStatus.NOT_VALID;
        }

        if (startDateTime.isAfter(now)) {
            return GPValidResponse.CertificateStatus.NOT_VALID_YET;
        }

        if (now.isAfter(endDateTime)) {
            return GPValidResponse.CertificateStatus.NOT_VALID;
        }

        return validationScanMode == ValidationScanMode.NORMAL_DGP ? GPValidResponse.CertificateStatus.VALID : GPValidResponse.CertificateStatus.NOT_VALID;

    }

}
