package com.bagnoli.verificac19.service.validationlogic;

import static com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID;
import static com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID_YET;
import static com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus.TEST_NEEDED;
import static com.bagnoli.verificac19.dto.GPValidResponse.CertificateStatus.VALID;
import static com.bagnoli.verificac19.dto.ValidationScanMode.BOOSTER_DGP;

import java.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;

import com.bagnoli.verificac19.dto.GPValidResponse;
import com.bagnoli.verificac19.dto.GPValidResponse.PersonData;
import com.bagnoli.verificac19.dto.ValidationScanMode;
import com.bagnoli.verificac19.exception.EmptyDigitalCovidCertificateException;
import com.bagnoli.verificac19.service.downloaders.SettingsRetriever;

import lombok.RequiredArgsConstructor;
import se.digg.dgc.payload.v1.DigitalCovidCertificate;
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

    @Override
    public GPValidResponse calculateValidity(DigitalCovidCertificate digitalCovidCertificate,
        ValidationScanMode validationScanMode) {
        VaccinationEntry vaccinationEntry = digitalCovidCertificate.getV().stream()
            .reduce((first, second) -> second)
            .orElseThrow(() -> new EmptyDigitalCovidCertificateException("No vaccines found"));
        PersonData personData = PersonData.builder()
            .name(digitalCovidCertificate.getNam().getGn())
            .surname(digitalCovidCertificate.getNam().getFn())
            .birthDate(digitalCovidCertificate.getDateOfBirth().asLocalDate())
            .build();

        String vaccinationType = vaccinationEntry.getMp();
        Integer doseNumber = vaccinationEntry.getDn();
        LocalDate vaccinationDate = vaccinationEntry.getDt();
        Integer totalSeriesOfDoses = vaccinationEntry.getSd();
        LocalDate now = LocalDate.now();

        // Check if vaccine is present in setting list otherwise returns not valid
        if (settingsRetriever.getSettingValue(VACCINE_END_DAY_COMPLETE, vaccinationType) == null) {
            return GPValidResponse.builder()
                .certificateStatus(NOT_VALID)
                .personData(personData)
                .build();
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
            return GPValidResponse.builder()
                .certificateStatus(NOT_VALID_YET)
                .personData(personData)
                .build();
        }
        if (now.isAfter(endDate)) {
            return GPValidResponse.builder()
                .certificateStatus(NOT_VALID)
                .personData(personData)
                .build();
        } else {
            //If the basic controls are passed, we have to check for booster validity with particular attention to johnson vaccine type
            if (validationScanMode == BOOSTER_DGP &&
                testNeeded(vaccinationType, doseNumber, totalSeriesOfDoses)) {
                return GPValidResponse.builder()
                    .certificateStatus(TEST_NEEDED)
                    .personData(personData)
                    .build();
            }
            return GPValidResponse.builder()
                .certificateStatus(VALID)
                .personData(personData)
                .build();
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
