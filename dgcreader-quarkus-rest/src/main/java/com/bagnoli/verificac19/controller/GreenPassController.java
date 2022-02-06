package com.bagnoli.verificac19.controller;

import static com.bagnoli.verificac19.dto.ValidationScanMode.Constants.NORMAL_DGP_VALUE;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.jaxrs.FormParam;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import com.bagnoli.verificac19.dto.GPValidResponse;
import com.bagnoli.verificac19.dto.ValidationScanMode;
import com.bagnoli.verificac19.service.GreenPassService;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Path("/v1/green-pass")
@RequiredArgsConstructor
public class GreenPassController {

    private final GreenPassService greenPassService;

    @GET
    @Path("/validate")
    @Produces(MediaType.APPLICATION_JSON)
    public GPValidResponse validateString(
        @NonNull @QueryParam("base45") String base45EncodedGP,
        @DefaultValue(NORMAL_DGP_VALUE) @QueryParam("validationScanMode")
            ValidationScanMode validationScanMode) {
        return this.greenPassService.validate(base45EncodedGP, validationScanMode);
    }

    @POST
    @Path("/validate-image")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public GPValidResponse validateImage(
        @MultipartForm Form form,
        @DefaultValue(NORMAL_DGP_VALUE) @QueryParam("validationScanMode")
            ValidationScanMode validationScanMode) {
        return this.greenPassService.validate(form.getQrCode(), validationScanMode);
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Form {

        @FormParam("file")
        @PartType(APPLICATION_OCTET_STREAM)
        private byte[] qrCode;

    }

}
