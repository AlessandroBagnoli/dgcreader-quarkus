package com.github.alessandrobagnoli.verificac19.exceptionmapper;

import static java.util.Collections.singletonList;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import java.time.Instant;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.github.alessandrobagnoli.verificac19.dto.ApiErrorSchema;

import lombok.RequiredArgsConstructor;

@Provider
@RequiredArgsConstructor
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

    @Context
    private final UriInfo uriInfo;

    @Override
    public Response toResponse(RuntimeException ex) {
        return Response
            .status(INTERNAL_SERVER_ERROR)
            .entity(ApiErrorSchema.builder()
                .errors(singletonList(ex.getMessage()))
                .path(uriInfo.getPath())
                .timestamp(Instant.now())
                .status(INTERNAL_SERVER_ERROR)
                .build())
            .build();
    }
    
}
