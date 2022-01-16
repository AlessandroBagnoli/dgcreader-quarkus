package com.bagnoli.verificac19.service.validationlogic;

import java.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;

import com.bagnoli.verificac19.dto.GPValidResponse;
import com.bagnoli.verificac19.dto.GPValidResponse.PersonData;
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

    private final SettingsRetriever settingsRetriever;

    @Override
    public GPValidResponse calculateValidity(DigitalCovidCertificate digitalCovidCertificate) {
        VaccinationEntry vaccinationEntry =
            digitalCovidCertificate.getV().stream().findFirst().orElseThrow();
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

        if (settingsRetriever.getSettingValue(VACCINE_END_DAY_COMPLETE, vaccinationType) == null) {
            return GPValidResponse.builder()
                .valid(false)
                .errorDescription("Vaccine settings not found for vaccine " + vaccinationType)
                .personData(personData)
                .build();
        }

        LocalDate startDate;
        LocalDate endDate;

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
                .valid(false)
                .errorDescription("Not valid yet")
                .personData(personData)
                .build();
        }
        if (now.isAfter(endDate)) {
            return GPValidResponse.builder()
                .valid(false)
                .errorDescription("Expired")
                .personData(personData)
                .build();
        }
        return GPValidResponse.builder()
            .valid(true)
            .personData(personData)
            .validUntil(endDate)
            .build();
    }
}
