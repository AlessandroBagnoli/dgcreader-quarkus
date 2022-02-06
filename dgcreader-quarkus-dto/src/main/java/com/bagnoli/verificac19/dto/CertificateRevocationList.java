package com.bagnoli.verificac19.dto;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class CertificateRevocationList {

    private Long chunk;
    private String creationDate;
    private Delta delta;
    private String firstElementInChunk;
    private String id;
    private Long lastChunk;
    private String lastElementInChunk;
    private List<String> revokedUcvi;
    private Long sizeSingleChunkInByte;
    private Long fromVersion;
    private Long version;
    private Long totalNumberUCVI;

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder(toBuilder = true)
    public static class Delta {

        private List<String> deletions;
        private List<String> insertions;

    }
}
