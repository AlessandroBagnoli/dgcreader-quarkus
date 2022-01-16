package com.bagnoli.verificac19.annotation;

import java.lang.reflect.Parameter;

import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import com.bagnoli.verificac19.exception.RequiredParamException;

@Provider
public class RequiredParameterFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        for (Parameter parameter : resourceInfo.getResourceMethod().getParameters()) {
            QueryParam queryAnnotation = parameter.getAnnotation(QueryParam.class);
            if (queryAnnotation != null && parameter.isAnnotationPresent(Required.class)
                && !requestContext.getUriInfo().getQueryParameters()
                .containsKey(queryAnnotation.value())) {
                throw new RequiredParamException(queryAnnotation.value() + " required");
            }
        }
    }
}
