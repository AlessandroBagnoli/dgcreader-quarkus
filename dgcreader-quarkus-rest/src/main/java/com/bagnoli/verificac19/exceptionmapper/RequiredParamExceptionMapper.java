package com.bagnoli.verificac19.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.bagnoli.verificac19.exception.RequiredParamException;

@Provider
public class RequiredParamExceptionMapper implements ExceptionMapper<RequiredParamException> {
    @Override
    public Response toResponse(RequiredParamException ex) {
        return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(ex.toString())
            .build();
    }
}
