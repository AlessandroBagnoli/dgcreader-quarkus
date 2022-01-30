package com.bagnoli.verificac19.controller;

import static com.bagnoli.verificac19.dto.ValidationScanMode.Constants.NORMAL_DGP_VALUE;

import java.util.Set;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.bagnoli.verificac19.dto.GPValidResponse;
import com.bagnoli.verificac19.dto.Setting;
import com.bagnoli.verificac19.dto.ValidationScanMode;
import com.bagnoli.verificac19.service.GreenPassService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Path("/v1/green-pass")
@RequiredArgsConstructor
public class GreenPassController {

    private final GreenPassService greenPassService;

    @GET
    @Path("/validate")
    @Produces(MediaType.APPLICATION_JSON)
    public GPValidResponse validate(
        @NonNull @QueryParam("base45") String base45EncodedGP,
        @DefaultValue(NORMAL_DGP_VALUE) @QueryParam("validationScanMode")
            ValidationScanMode validationScanMode) {
        return this.greenPassService.validate(base45EncodedGP, validationScanMode);
    }

    @GET
    @Path("/settings")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<Setting> getSettings() {
        return this.greenPassService.getSettings();
    }

    @GET
    @Path("/certificates")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getCertificates() {
        return this.greenPassService.getCertificates();
    }

    @GET
    @Path("/kids")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getKids() {
        return this.greenPassService.getKids();
    }

}
