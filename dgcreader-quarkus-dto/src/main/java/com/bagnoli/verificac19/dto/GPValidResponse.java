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

    private boolean valid;
    private String errorDescription;
    private PersonData personData;
    private LocalDate validUntil;

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder
    public static class PersonData {

        private String name;
        private String surname;
        private LocalDate birthDate;

    }


}
