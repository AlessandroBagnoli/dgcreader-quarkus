package com.bagnoli.verificac19.controller;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.bagnoli.verificac19.annotation.Required;
import com.bagnoli.verificac19.dto.GPValidResponse;
import com.bagnoli.verificac19.dto.Setting;
import com.bagnoli.verificac19.service.GreenPassService;

import lombok.RequiredArgsConstructor;

@Path("/v1/green-pass")
@RequiredArgsConstructor
public class GreenPassController {

    private final GreenPassService greenPassService;

    @GET
    @Path("/validate")
    @Produces(MediaType.APPLICATION_JSON)
    public GPValidResponse validate(
        @Required @QueryParam("base45") String base45EncodedGP
    ) {
        return this.greenPassService.validate(base45EncodedGP);
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
