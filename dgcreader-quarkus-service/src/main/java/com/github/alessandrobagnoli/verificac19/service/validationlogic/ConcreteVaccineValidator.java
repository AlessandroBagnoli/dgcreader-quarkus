package com.github.alessandrobagnoli.verificac19.service.validationlogic;

import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID;
import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID_YET;
import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.TEST_NEEDED;
import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.VALID;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.BOOSTER_DGP;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import com.github.alessandrobagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate;
import com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus;
import com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode;
import com.github.alessandrobagnoli.verificac19.exception.EmptyDigitalCovidCertificateException;
import com.github.alessandrobagnoli.verificac19.service.downloaders.SettingsRetriever;

import lombok.RequiredArgsConstructor;
import se.digg.dgc.payload.v1.VaccinationEntry;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteVaccineValidator implements VaccineValidator {

    private static final String VACCINE_START_DAY_NOT_COMPLETE = "vaccine_start_day_not_complete";
    private static final String VACCINE_END_DAY_NOT_COMPLETE = "vaccine_end_day_not_complete";
    private static final String VACCINE_START_DAY_COMPLETE = "vaccine_start_day_complete";
    private static final String VACCINE_END_DAY_COMPLETE = "vaccine_end_day_complete";
    private static final String JOHNSON = "EU/1/20/1525";

    private final SettingsRetriever settingsRetriever;
    private final RevokedAndBlacklistedChecker revokedAndBlacklistedChecker;

    @Override
    public CertificateStatus calculateValidity(
        EnrichedDigitalCovidCertificate digitalCovidCertificate,
        ValidationScanMode validationScanMode) {
        VaccinationEntry vaccinationEntry = digitalCovidCertificate.getV().stream()
            .reduce((first, second) -> second)
            .orElseThrow(() -> new EmptyDigitalCovidCertificateException("No vaccines found"));

        String vaccinationType = vaccinationEntry.getMp();
        Integer doseNumber = vaccinationEntry.getDn();
        LocalDate vaccinationDate = vaccinationEntry.getDt();
        Integer totalSeriesOfDoses = vaccinationEntry.getSd();
        LocalDate now = LocalDate.now();
        String certificateIdentifier = vaccinationEntry.getCi();

        Optional<CertificateStatus> check =
            revokedAndBlacklistedChecker.check(certificateIdentifier);
        if (check.isPresent()) {
            return check.get();
        }

        // Check if vaccine is present in setting list otherwise returns not valid
        if (settingsRetriever.getSettingValue(VACCINE_END_DAY_COMPLETE, vaccinationType) == null) {
            return NOT_VALID;
        }

        LocalDate startDate;
        LocalDate endDate;

        //Calculate start date and end date of validity based on the vaccination type and dose number
        if (doseNumber < totalSeriesOfDoses) {
            startDate = vaccinationDate.plusDays(
                settingsRetriever.getSettingValue(VACCINE_START_DAY_NOT_COMPLETE,
                    vaccinationType));
            endDate = vaccinationDate.plusDays(
                settingsRetriever.getSettingValue(VACCINE_END_DAY_NOT_COMPLETE, vaccinationType));
        } else {
            startDate = vaccinationDate.plusDays(
                settingsRetriever.getSettingValue(VACCINE_START_DAY_COMPLETE, vaccinationType));
            endDate = vaccinationDate.plusDays(
                settingsRetriever.getSettingValue(VACCINE_END_DAY_COMPLETE, vaccinationType));
        }

        if (startDate.isAfter(now)) {
            return NOT_VALID_YET;
        }
        if (now.isAfter(endDate)) {
            return NOT_VALID;
        } else {
            //If the basic controls are passed, we have to check for booster validity with particular attention to johnson vaccine type
            if (validationScanMode == BOOSTER_DGP &&
                testNeeded(vaccinationType, doseNumber, totalSeriesOfDoses)) {
                return TEST_NEEDED;
            }
            return VALID;
        }
    }

    private boolean testNeeded(String vaccinationType, Integer doseNumber,
        Integer totalSeriesOfDoses) {
        if (vaccinationType.equals(JOHNSON)) {
            return doseNumber.equals(totalSeriesOfDoses) && doseNumber < 2;
        } else {
            return doseNumber.equals(totalSeriesOfDoses) && doseNumber < 3;
        }
    }
}
