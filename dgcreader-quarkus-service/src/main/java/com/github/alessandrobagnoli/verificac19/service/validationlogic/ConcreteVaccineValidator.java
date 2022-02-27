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
    private static final String JOHNSON = "EU/1/20/1525";
    private static final String SPUTNIK = "Sputnik-V";
    private static final String PARTIAL = "PARTIAL";
    private static final String RECALL = "RECALL";
    private static final String CYCLE = "CYCLE";
    private static final String EMA_VACCINES = "EMA_vaccines";
    private static final String TYPE = "GENERIC";
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

        VaccineValidityRange vaccineValidityRange =
            calculateValidityRange(doseNumber, totalSeriesOfDoses, vaccinationType,
                vaccinationDate);

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
            return RECALL.equals(type) ? VALID : TEST_NEEDED;
        }
        if ((validationScanMode == WORK_DGP && personAge >= 50)
            || validationScanMode == ENHANCED_DGP) {
            if (!CYCLE.equals(type)) {
                return VALID;
            }
            return daysActive >= 180 && !"IT".equals(country) ? TEST_NEEDED : VALID;
        }
        //TODO verificare
        return VALID;
    }

    private String calculateType(Integer doseNumber, Integer totalSeriesOfDoses,
        String vaccinationType) {
        if (doseNumber < totalSeriesOfDoses) {
            return PARTIAL;
        }
        if (doseNumber > totalSeriesOfDoses) {
            return RECALL;
        }
        if ((JOHNSON.equals(vaccinationType) && doseNumber >= 2)
            || !JOHNSON.equals(vaccinationType) && doseNumber >= 3) {
            return RECALL;
        } else {
            return CYCLE;
        }
    }

    private boolean isEMA(String vaccinationType, String country) {
        String values = settingsRetriever.getSettingValueAsString(EMA_VACCINES, TYPE);
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
        if (validationScanMode == WORK_DGP
            || validationScanMode == RSA_VISITORS_DGP
            || (validationScanMode == ENHANCED_DGP && !PARTIAL.equals(type))) {
            return TEST_NEEDED;
        }
        return NOT_VALID;
    }

    private VaccineValidityRange calculateValidityRange(Integer doseNumber,
        Integer totalSeriesOfDoses, String vaccinationType, LocalDate vaccinationDate) {
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
        return VaccineValidityRange.builder()
            .startDate(startDate)
            .endDate(endDate)
            .build();
    }

}
