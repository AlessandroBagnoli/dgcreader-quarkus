package com.bagnoli.verificac19.exceptionmapper;

import java.security.SignatureException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class SignatureExceptionMapper implements ExceptionMapper<SignatureException> {
    @Override
    public Response toResponse(SignatureException ex) {
        return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(ex.toString())
            .build();
    }
}
