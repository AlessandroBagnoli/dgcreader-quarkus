package com.github.alessandrobagnoli.verificac19.exceptionmapper;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {
    @Override
    public Response toResponse(NotFoundException ex) {
        return Response
            .status(Response.Status.NOT_FOUND)
            .entity(ex.toString()) //TODO creare un DTO universale da restituire in caso di errore
            .build();
    }
}
