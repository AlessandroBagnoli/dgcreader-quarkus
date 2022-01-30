package com.bagnoli.verificac19.service;

import java.util.Set;

import com.bagnoli.verificac19.dto.GPValidResponse;
import com.bagnoli.verificac19.dto.Setting;

public interface GreenPassService {

    GPValidResponse validate(String base45, boolean includeValidUntil);

    GPValidResponse validate(String base45);

    Set<Setting> getSettings();

    Set<String> getCertificates();

    Set<String> getKids();

}
