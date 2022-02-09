package com.github.alessandrobagnoli.verificac19.dto;

import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.Constants.BOOSTER_DGP_VALUE;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.Constants.NORMAL_DGP_VALUE;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.Constants.SUPER_DGP_VALUE;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

@RequiredArgsConstructor
@Getter
public enum ValidationScanMode {
    NORMAL_DGP(NORMAL_DGP_VALUE),
    SUPER_DGP(SUPER_DGP_VALUE),
    BOOSTER_DGP(BOOSTER_DGP_VALUE);

    private final String value;

    @UtilityClass
    public static class Constants {
        public static final String NORMAL_DGP_VALUE = "NORMAL_DGP";
        public static final String SUPER_DGP_VALUE = "SUPER_DGP";
        public static final String BOOSTER_DGP_VALUE = "BOOSTER_DGP";
    }

}
