package com.bagnoli.verificac19.service;

import java.util.List;
import java.util.Set;

import com.bagnoli.verificac19.dto.GPValidResponse;
import com.bagnoli.verificac19.dto.Setting;

public interface GreenPassService {

    GPValidResponse validate(String base45, boolean includeValidUntil);

    Set<Setting> getSettings();

    Set<String> getCertificates();

    List<String> getKids();

}
