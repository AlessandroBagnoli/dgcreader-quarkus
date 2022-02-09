package com.github.alessandrobagnoli.verificac19.config;

import javax.enterprise.context.ApplicationScoped;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.jackson.ObjectMapperCustomizer;

@ApplicationScoped
public class CustomObjectMapper implements ObjectMapperCustomizer {

    @Override
    public void customize(ObjectMapper objectMapper) {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

}
