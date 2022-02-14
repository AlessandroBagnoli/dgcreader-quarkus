package com.github.alessandrobagnoli.verificac19.service;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.net.URL;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.http.ssl.SSLContextBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import com.github.alessandrobagnoli.verificac19.dto.CertificateRevocationList;
import com.github.alessandrobagnoli.verificac19.dto.CrlStatus;
import com.github.alessandrobagnoli.verificac19.dto.Setting;

import io.netty.handler.ssl.SslProtocols;
import lombok.SneakyThrows;

@Produces(APPLICATION_JSON)
@Path("/v1/dgc")
public interface DGCApiService {

    class RestClientConfig {

        @ConfigProperty(name = "settings-api.baseurl")
        String baseUrl;

        @SneakyThrows
        @javax.enterprise.inject.Produces
        public DGCApiService dgcApiService() {
            SSLContext sslContext = SSLContextBuilder.create()
                .setProtocol(SslProtocols.TLS_v1_2)
                .build();
            return RestClientBuilder.newBuilder()
                .baseUrl(new URL(baseUrl))
                .sslContext(sslContext)
                .build(DGCApiService.class);
        }

    }

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
