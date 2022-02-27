package com.github.alessandrobagnoli.verificac19.service.validationlogic;

import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID;
import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID_YET;
import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.TEST_NEEDED;
import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.VALID;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.BASE_DGP;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.ENHANCED_DGP;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.IT_ENTRY_DGP;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.RSA_VISITORS_DGP;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.WORK_DGP;
import static java.util.Optional.ofNullable;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import com.github.alessandrobagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate;
import com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus;
import com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode;
import com.github.alessandrobagnoli.verificac19.exception.EmptyDigitalCovidCertificateException;
import com.github.alessandrobagnoli.verificac19.service.downloaders.SettingsRetriever;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import se.digg.dgc.payload.v1.VaccinationEntry;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteVaccineValidator implements VaccineValidator {

    private static final String VACCINE_START_DAY_NOT_COMPLETE = "vaccine_start_day_not_complete";
    private static final String VACCINE_END_DAY_NOT_COMPLETE = "vaccine_end_day_not_complete";
    private static final String VACCINE_START_DAY_COMPLETE = "vaccine_start_day_complete";
    private static final String VACCINE_END_DAY_COMPLETE = "vaccine_end_day_complete";
    private static final String VACCINE_START_DAY_COMPLETE_IT = "vaccine_start_day_complete_IT";
    private static final String VACCINE_END_DAY_COMPLETE_IT = "vaccine_end_day_complete_IT";
    private static final String VACCINE_START_DAY_BOOSTER_IT = "vaccine_start_day_booster_IT";
    private static final String VACCINE_END_DAY_BOOSTER_IT = "vaccine_end_day_booster_IT";
    private static final String VACCINE_START_DAY_COMPLETE_NOT_IT =
        "vaccine_start_day_complete_NOT_IT";
    private static final String VACCINE_END_DAY_COMPLETE_NOT_IT = "vaccine_end_day_complete_NOT_IT";
    private static final String VACCINE_START_DAY_BOOSTER_NOT_IT =
        "vaccine_start_day_booster_NOT_IT";
    private static final String VACCINE_END_DAY_BOOSTER_NOT_IT = "vaccine_end_day_booster_NOT_IT";
    private static final String VACCINE_END_DAY_COMPLETE_EXTENDED_EMA =
        "vaccine_end_day_complete_extended_EMA";
    private static final String JOHNSON = "EU/1/20/1525";
    private static final String SPUTNIK = "Sputnik-V";
    private static final String PARTIAL = "PARTIAL";
    private static final String CYCLE = "CYCLE";
    private static final String BOOSTER = "BOOSTER";
    private static final String EMA_VACCINES = "EMA_vaccines";
    private static final String SETTING_TYPE = "GENERIC";
    private static final String COUNTRY_SAN_MARINO = "SM";

    private final SettingsRetriever settingsRetriever;
    private final RevokedAndBlacklistedChecker revokedAndBlacklistedChecker;

    @Value
    @Builder
    static class VaccineValidityRange {
        LocalDate startDate;
        LocalDate endDate;
    }

    @Override
    public CertificateStatus calculateValidity(
        EnrichedDigitalCovidCertificate digitalCovidCertificate,
        ValidationScanMode validationScanMode) {
        VaccinationEntry vaccinationEntry = digitalCovidCertificate.getV().stream()
            .findFirst()
            .orElseThrow(() -> new EmptyDigitalCovidCertificateException("No vaccines found"));

        String vaccinationType = vaccinationEntry.getMp();
        Integer doseNumber = vaccinationEntry.getDn();
        LocalDate vaccinationDate = vaccinationEntry.getDt();
        Integer totalSeriesOfDoses = vaccinationEntry.getSd();
        String country = vaccinationEntry.getCo();
        String certificateIdentifier = vaccinationEntry.getCi();
        LocalDate dateOfBirth = digitalCovidCertificate.getDateOfBirth().asLocalDate();
        LocalDate now = LocalDate.now();
        long personAge = ChronoUnit.YEARS.between(dateOfBirth, now);

        Optional<CertificateStatus> check =
            revokedAndBlacklistedChecker.check(certificateIdentifier);
        if (check.isPresent()) {
            return check.get();
        }

        // Check if vaccine is present in setting list otherwise returns not valid
        if (settingsRetriever.getSettingValue(VACCINE_END_DAY_COMPLETE, vaccinationType) == null) {
            return NOT_VALID;
        }

        String type = calculateType(doseNumber, totalSeriesOfDoses, vaccinationType);

        VaccineValidityRange vaccineValidityRange = calculateValidityRange(type, vaccinationType,
            vaccinationDate, country);

        if (vaccineValidityRange.getStartDate().isAfter(now)) {
            return NOT_VALID_YET;
        }
        if (now.isAfter(vaccineValidityRange.getEndDate())) {
            return NOT_VALID;
        }

        long daysActive = ChronoUnit.DAYS.between(vaccineValidityRange.getStartDate(), now);

        return isEMA(vaccinationType, country) ?
            calculateValidityForEMA(validationScanMode, personAge, type, country, daysActive) :
            calculateValidityForNotEMA(validationScanMode, personAge, type);

    }

    private CertificateStatus calculateValidityForEMA(ValidationScanMode validationScanMode,
        long personAge, String type, String country, long daysActive) {
        if (validationScanMode == RSA_VISITORS_DGP && !PARTIAL.equals(type)) {
            return BOOSTER.equals(type) ? VALID : TEST_NEEDED;
        }
        if ((validationScanMode == WORK_DGP && personAge >= 50)
            || validationScanMode == ENHANCED_DGP) {
            if (!CYCLE.equals(type)) {
                return VALID;
            }
            return daysActive >= 180 && !"IT".equals(country) ? TEST_NEEDED : VALID;
        }
        if (validationScanMode == IT_ENTRY_DGP && !type.equals(PARTIAL)
            || validationScanMode == WORK_DGP || validationScanMode == BASE_DGP) {
            return VALID;
        }
        return NOT_VALID;
    }

    private String calculateType(Integer doseNumber, Integer totalSeriesOfDoses,
        String vaccinationType) {
        if (isPartial(doseNumber, totalSeriesOfDoses)) {
            return PARTIAL;
        }
        return isBooster(doseNumber, totalSeriesOfDoses, vaccinationType) ? BOOSTER : CYCLE;
    }

    private boolean isBooster(Integer doseNumber, Integer totalSeriesOfDoses,
        String vaccinationType) {
        return doseNumber > totalSeriesOfDoses
            || (JOHNSON.equals(vaccinationType) && doseNumber >= 2)
            || (!JOHNSON.equals(vaccinationType) && doseNumber >= 3);
    }

    private boolean isPartial(Integer doseNumber, Integer totalSeriesOfDoses) {
        return doseNumber < totalSeriesOfDoses;
    }

    private boolean isEMA(String vaccinationType, String country) {
        String values = settingsRetriever.getSettingValueAsString(EMA_VACCINES, SETTING_TYPE);
        boolean isStandardEma = ofNullable(values)
            .map(s -> Arrays.asList(s.split(";")).contains(vaccinationType))
            .orElse(false);
        // Sputnik is EMA only in SAN MARINO
        boolean isEMASM =
            SPUTNIK.equals(vaccinationType) && COUNTRY_SAN_MARINO.equals(country);

        return isStandardEma || isEMASM;
    }

    private CertificateStatus calculateValidityForNotEMA(ValidationScanMode validationScanMode,
        long personAge, String type) {
        if (validationScanMode == BASE_DGP
            || validationScanMode == IT_ENTRY_DGP
            || (validationScanMode == WORK_DGP && personAge < 50)
            || (validationScanMode == RSA_VISITORS_DGP && PARTIAL.equals(type))) {
            return NOT_VALID;
        }
        if ((validationScanMode == WORK_DGP && !PARTIAL.equals(type))
            || validationScanMode == RSA_VISITORS_DGP
            || (validationScanMode == ENHANCED_DGP && !PARTIAL.equals(type))) {
            return TEST_NEEDED;
        }
        return NOT_VALID;
    }

    private VaccineValidityRange calculateValidityRange(String type, String vaccinationType,
        LocalDate vaccinationDate, String country) {
        LocalDate startDate;
        LocalDate endDate;
        switch (type) {
            case PARTIAL:
                startDate = vaccinationDate.plusDays(
                    settingsRetriever.getSettingValue(VACCINE_START_DAY_NOT_COMPLETE,
                        vaccinationType));
                endDate = vaccinationDate.plusDays(
                    settingsRetriever.getSettingValue(VACCINE_END_DAY_NOT_COMPLETE,
                        vaccinationType));
                break;
            case CYCLE:
                startDate = vaccinationDate.plusDays(
                    getVaccineStartDayCompleteByCountry(country, vaccinationType));
                endDate = vaccinationDate.plusDays(
                    getVaccineEndDayCompleteByCountry(country, vaccinationType));
                break;
            case BOOSTER:
            default:
                startDate = vaccinationDate.plusDays(getVaccineStartDayBoosterByCountry(country));
                endDate = vaccinationDate.plusDays(getVaccineEndDayBoosterByCountry(country));
                break;
        }
        return VaccineValidityRange.builder()
            .startDate(startDate)
            .endDate(endDate)
            .build();
    }

    private Integer getVaccineStartDayCompleteByCountry(String countryCode, String vaccineType) {
        int daysToAdd = settingsRetriever.getSettingValue(VACCINE_START_DAY_COMPLETE, vaccineType);
        return "IT".equals(countryCode) ?
            settingsRetriever.getSettingValue(VACCINE_START_DAY_COMPLETE_IT, SETTING_TYPE)
                + daysToAdd :
            settingsRetriever.getSettingValue(VACCINE_START_DAY_COMPLETE_NOT_IT, SETTING_TYPE)
                + daysToAdd;
    }

    private Integer getVaccineEndDayCompleteByCountry(String countryCode, String vaccineType) {
        int daysToAdd = settingsRetriever.getSettingValue(VACCINE_END_DAY_COMPLETE, vaccineType);
        return "IT".equals(countryCode) ?
            settingsRetriever.getSettingValue(VACCINE_END_DAY_COMPLETE_IT, SETTING_TYPE)
                + daysToAdd :
            settingsRetriever.getSettingValue(VACCINE_END_DAY_COMPLETE_NOT_IT, SETTING_TYPE)
                + daysToAdd;
    }

    private Integer getVaccineStartDayBoosterByCountry(String countryCode) {
        return "IT".equals(countryCode) ?
            settingsRetriever.getSettingValue(VACCINE_START_DAY_BOOSTER_IT, SETTING_TYPE) :
            settingsRetriever.getSettingValue(VACCINE_START_DAY_BOOSTER_NOT_IT, SETTING_TYPE);
    }

    private Integer getVaccineEndDayBoosterByCountry(String countryCode) {
        return "IT".equals(countryCode) ?
            settingsRetriever.getSettingValue(VACCINE_END_DAY_BOOSTER_IT, SETTING_TYPE) :
            settingsRetriever.getSettingValue(VACCINE_END_DAY_BOOSTER_NOT_IT, SETTING_TYPE);
    }

}
