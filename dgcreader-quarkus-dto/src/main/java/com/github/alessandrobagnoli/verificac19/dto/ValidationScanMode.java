package com.github.alessandrobagnoli.verificac19.dto;

import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.Constants.BASE_DGP_VALUE;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.Constants.ENHANCED_DGP_VALUE;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.Constants.IT_ENTRY_DGP_VALUE;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.Constants.RSA_VISITORS_DGP_VALUE;
import static com.github.alessandrobagnoli.verificac19.dto.ValidationScanMode.Constants.WORK_DGP_VALUE;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

@RequiredArgsConstructor
@Getter
public enum ValidationScanMode {

    BASE_DGP(BASE_DGP_VALUE),
    ENHANCED_DGP(ENHANCED_DGP_VALUE),
    RSA_VISITORS_DGP(RSA_VISITORS_DGP_VALUE),
    WORK_DGP(WORK_DGP_VALUE),
    IT_ENTRY_DGP(IT_ENTRY_DGP_VALUE);

    private final String value;

    @UtilityClass
    public static class Constants {
        public static final String BASE_DGP_VALUE = "BASE_DGP";
        public static final String ENHANCED_DGP_VALUE = "ENHANCED_DGP";
        public static final String RSA_VISITORS_DGP_VALUE = "RSA_VISITORS_DGP";
        public static final String WORK_DGP_VALUE = "WORK_DGP";
        public static final String IT_ENTRY_DGP_VALUE = "IT_ENTRY_DGP";
    }

}
