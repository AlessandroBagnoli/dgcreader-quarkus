package com.bagnoli.verificac19.dto;

import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GPValidResponse {

    private CertificateStatus certificateStatus;
    private PersonData personData;

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder
    public static class PersonData {

        private String name;
        private String surname;
        private LocalDate birthDate;

    }

    public enum CertificateStatus {
        NOT_VALID,
        NOT_VALID_YET, 
        VALID, 
        REVOKED, 
        NOT_EU_DCC, 
        TEST_NEEDED;
    }

}
