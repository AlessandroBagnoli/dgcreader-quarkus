package com.bagnoli.verificac19.service.restclient;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.bagnoli.verificac19.dto.CertificateRevocationList;
import com.bagnoli.verificac19.dto.CrlStatus;
import com.bagnoli.verificac19.dto.Setting;

@Produces(APPLICATION_JSON)
@Path("/v1/dgc")
public interface DGCApiService {

    @GET
    @Path("/settings")
    Set<Setting> getSettings();

    @GET
    @Path("/signercertificate/status")
    Set<String> getKids();

    @GET
    @Path("/signercertificate/update")
    Response getCertificates(@HeaderParam("X-RESUME-TOKEN") String resumeToken);

    @GET
    @Path("/drl/check")
    CrlStatus getCRLStatus(@QueryParam("version") Long version, @QueryParam("chunk") Long chunk);

    @GET
    @Path("/drl")
    CertificateRevocationList getCertificateRevocationList(@QueryParam("version") Long version,
        @QueryParam("chunk") Long chunk);

}
