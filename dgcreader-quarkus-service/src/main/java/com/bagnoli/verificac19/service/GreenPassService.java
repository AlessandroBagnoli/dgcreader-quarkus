package com.bagnoli.verificac19.service;

import java.util.Set;

import com.bagnoli.verificac19.dto.GPValidResponse;
import com.bagnoli.verificac19.dto.Setting;
import com.bagnoli.verificac19.dto.ValidationScanMode;

public interface GreenPassService {

    GPValidResponse validate(String base45, ValidationScanMode validationScanMode);

    Set<Setting> getSettings();

    Set<String> getCertificates();

    Set<String> getKids();

}
