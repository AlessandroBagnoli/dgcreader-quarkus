package com.bagnoli.verificac19.controller;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.bagnoli.verificac19.service.CacheService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Path("/v1/cache")
public class CacheController {

    private final CacheService cacheService;

    @DELETE
    @Path("/invalidate-all")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteCache() {
        cacheService.invalidateAll();
    }

}
