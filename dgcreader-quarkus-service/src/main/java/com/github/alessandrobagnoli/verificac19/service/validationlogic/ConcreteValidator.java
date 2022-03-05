package com.github.alessandrobagnoli.verificac19.service.validationlogic;

import static com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus.NOT_EU_DCC;
import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;

import com.github.alessandrobagnoli.verificac19.customdecoder.EnrichedDigitalCovidCertificate;
import com.github.alessandrobagnoli.verificac19.dto.GPValidResponse;
import com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.CertificateStatus;
import com.github.alessandrobagnoli.verificac19.dto.GPValidResponse.PersonData;
import com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode;

import lombok.RequiredArgsConstructor;
import se.digg.dgc.payload.v1.VaccinationEntry;

@ApplicationScoped
@RequiredArgsConstructor
public class ConcreteValidator implements Validator {

    private final VaccineValidator vaccineValidator;
    private final TestValidator testValidator;
    private final RecoveryValidator recoveryValidator;
    private final ExemptionValidator exemptionValidator;

    @Override
    public GPValidResponse validate(EnrichedDigitalCovidCertificate dgc,
        ValidationScanMode scanMode) {
        PersonData personData = PersonData.builder()
            .name(dgc.getNam().getGn())
            .surname(dgc.getNam().getFn())
            .birthDate(dgc.getDateOfBirth().asLocalDate())
            .build();

        return GPValidResponse.builder()
            .personData(personData)
            .certificateStatus(ofNullable(dgc.getV())
                .map(validateVaccine(dgc, scanMode))
                .or(validateTest(dgc, scanMode))
                .or(validateRecovery(dgc, scanMode))
                .or(validateExemption(dgc, scanMode))
                .orElse(NOT_EU_DCC))
            .build();
    }

    private Function<List<VaccinationEntry>, CertificateStatus> validateVaccine(
        EnrichedDigitalCovidCertificate dgc, ValidationScanMode scanMode) {
        return x -> vaccineValidator.calculateValidity(dgc, scanMode);
    }

    private Supplier<Optional<CertificateStatus>> validateTest(
        EnrichedDigitalCovidCertificate dgc, ValidationScanMode scanMode) {
        return () -> ofNullable(dgc.getT())
            .map(x -> testValidator.calculateValidity(dgc, scanMode));
    }

    private Supplier<Optional<CertificateStatus>> validateRecovery(
        EnrichedDigitalCovidCertificate dgc, ValidationScanMode scanMode) {
        return () -> ofNullable(dgc.getR())
            .map(x -> recoveryValidator.calculateValidity(dgc, scanMode));
    }

    private Supplier<Optional<CertificateStatus>> validateExemption(
        EnrichedDigitalCovidCertificate dgc, ValidationScanMode scanMode) {
        return () -> ofNullable(dgc.getE())
            .map(x -> exemptionValidator.calculateValidity(dgc, scanMode));
    }

}
