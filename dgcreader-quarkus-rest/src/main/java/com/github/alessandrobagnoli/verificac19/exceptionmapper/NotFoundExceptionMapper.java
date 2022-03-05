package com.github.alessandrobagnoli.verificac19.exceptionmapper;

import static java.util.Collections.singletonList;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.time.Instant;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.github.alessandrobagnoli.verificac19.dto.ApiErrorSchema;

import lombok.RequiredArgsConstructor;

@Provider
@RequiredArgsConstructor
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Context
    private final UriInfo uriInfo;

    @Override
    public Response toResponse(NotFoundException ex) {
        return Response
            .status(NOT_FOUND)
            .entity(ApiErrorSchema.builder()
                .errors(singletonList(ex.getMessage()))
                .path(uriInfo.getPath())
                .timestamp(Instant.now())
                .status(NOT_FOUND)
                .build())
            .build();
    }

}
