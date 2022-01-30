package com.bagnoli.verificac19.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class CrlStatus {
    
    private Long fromVersion;
    private String id;
    private Long chunk;
    private Long numDiAdd;
    private Long numDiDelete;
    private Long sizeSingleChunkInByte;
    private Long totalSizeInByte;
    private Long version;
    private Long totalChunk;
    private Long totalNumberUCVI;
    
}
