package com.bagnoli.verificac19.service;

import com.bagnoli.verificac19.dto.GPValidResponse;
import com.bagnoli.verificac19.dto.ValidationScanMode;

public interface GreenPassService {

    GPValidResponse validate(String base45, ValidationScanMode validationScanMode);

    GPValidResponse validate(byte[] file, ValidationScanMode validationScanMode);

}
