package com.github.alessandrobagnoli.verificac19.service.validationlogic;

import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID;
import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_VALID_YET;
import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.TEST_NEEDED;
import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.VALID;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.ENHANCED_DGP;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.IT_ENTRY_DGP;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.RSA_VISITORS_DGP;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.WORK_DGP;
import static java.util.Optional.ofNullable;

import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import com.github.alessandrobagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate;
import com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus;
import com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode;
import com.github.alessandrobagnoli.verificac19.exception.EmptyDigitalCovidCertificateException;
import com.github.alessandrobagnoli.verificac19.service.ConcreteGDCDecoderWrapper;
import com.github.alessandrobagnoli.verificac19.service.downloaders.SettingsRetriever;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import se.digg.dgc.payload.v1.RecoveryEntry;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteRecoveryValidator implements RecoveryValidator {

    private static final String OID_RECOVERY = "1.3.6.1.4.1.1847.2021.1.3";
    private static final String OID_ALT_RECOVERY = "1.3.6.1.4.1.0.1847.2021.1.3";
    private static final String RECOVERY_PV_CERT_START_DAY = "recovery_pv_cert_start_day";
    private static final String RECOVERY_PV_CERT_END_DAY = "recovery_pv_cert_end_day";
    private static final String RECOVERY_CERT_START_DAY_IT = "recovery_cert_start_day_IT";
    private static final String RECOVERY_CERT_END_DAY_IT = "recovery_cert_end_day_IT";
    private static final String RECOVERY_CERT_START_DAY_NOT_IT = "recovery_cert_start_day_NOT_IT";
    private static final String RECOVERY_CERT_END_DAY_NOT_IT = "recovery_cert_end_day_NOT_IT";

    private static final String SETTING_TYPE = "GENERIC";

    private final RevokedAndBlacklistedChecker revokedAndBlacklistedChecker;
    private final SettingsRetriever settingsRetriever;
    private final ConcreteGDCDecoderWrapper decoderWrapper;

    @Override
    public CertificateStatus calculateValidity(
        EnrichedDigitalCovidCertificate digitalCovidCertificate,
        ValidationScanMode validationScanMode) {
        RecoveryEntry recoveryEntry = digitalCovidCertificate.getR().stream()
            .findFirst()
            .orElseThrow(() -> new EmptyDigitalCovidCertificateException("No recoveries found"));

        String certificateIdentifier = recoveryEntry.getCi();
        Optional<CertificateStatus> check =
            revokedAndBlacklistedChecker.check(certificateIdentifier);
        if (check.isPresent()) {
            return check.get();
        }

        boolean recoveryBis = isRecoveryBis(digitalCovidCertificate.getR());

        String countryCode = validationScanMode == IT_ENTRY_DGP ? recoveryEntry.getCo() : "IT";

        Integer endDaysToAdd =
            recoveryBis ? getRecoveryCertPvEndDay() : getRecoveryCertEndDayUnified(countryCode);
        Integer startDaysToAdd =
            recoveryBis ? getRecoveryCertPVStartDay() : getRecoveryCertStartDayUnified(countryCode);

        LocalDate certificateValidFrom = recoveryEntry.getDf().plusDays(startDaysToAdd);
        LocalDate certificateValidUntil = certificateValidFrom.plusDays(endDaysToAdd);
        LocalDate now = LocalDate.now();

        if (certificateValidFrom.isAfter(now)) {
            return NOT_VALID_YET;
        }

        if (now.isAfter(certificateValidUntil)) {
            return NOT_VALID;
        }

        long daysActive = ChronoUnit.DAYS.between(certificateValidFrom, now);

        return validationScanMode == RSA_VISITORS_DGP ?
            isTestNeededForRSAVisitors(recoveryBis) :
            evaluateOtherCases(validationScanMode, countryCode, daysActive);
    }

    private CertificateStatus evaluateOtherCases(ValidationScanMode validationScanMode,
        String countryCode, long daysActive) {
        return (validationScanMode == ENHANCED_DGP || validationScanMode == WORK_DGP) && (
            !"IT".equals(countryCode) && daysActive >= 180) ? TEST_NEEDED : VALID;
    }

    @SneakyThrows
    private boolean isRecoveryBis(List<RecoveryEntry> recoveries) {
        X509Certificate cert = decoderWrapper.getCurrentCertificate();
        RecoveryEntry firstRecovery = recoveries.stream().findFirst().orElseThrow();
        return "IT".equals(firstRecovery.getCo()) &&
            ofNullable(cert.getExtendedKeyUsage())
                .map(eku -> eku.stream()
                    .anyMatch(s -> OID_RECOVERY.equals(s) || OID_ALT_RECOVERY.equals(s)))
                .orElse(false);
    }

    private Integer getRecoveryCertPVStartDay() {
        return settingsRetriever.getSettingValue(RECOVERY_PV_CERT_START_DAY, SETTING_TYPE);
    }

    private Integer getRecoveryCertPvEndDay() {
        return settingsRetriever.getSettingValue(RECOVERY_PV_CERT_END_DAY, SETTING_TYPE);
    }

    private Integer getRecoveryCertStartDayUnified(String countryCode) {
        return "IT".equals(countryCode) ?
            settingsRetriever.getSettingValue(RECOVERY_CERT_START_DAY_IT, SETTING_TYPE) :
            settingsRetriever.getSettingValue(RECOVERY_CERT_START_DAY_NOT_IT, SETTING_TYPE);
    }

    private Integer getRecoveryCertEndDayUnified(String countryCode) {
        return "IT".equals(countryCode) ?
            settingsRetriever.getSettingValue(RECOVERY_CERT_END_DAY_IT, SETTING_TYPE) :
            settingsRetriever.getSettingValue(RECOVERY_CERT_END_DAY_NOT_IT, SETTING_TYPE);
    }

    private CertificateStatus isTestNeededForRSAVisitors(boolean recoveryBis) {
        return recoveryBis ? VALID : TEST_NEEDED;
    }

}
