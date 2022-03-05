package com.github.alessandrobagnoli.verificac19.service.downloaders;

import lombok.Builder;
import lombok.Data;

public interface DrlStatusStore {

    @Data
    @Builder
    class DrlStatus {
        private final Long version;
        private final Long chunk;
    }

    DrlStatus getDrlStatus();

    void setDrlStatus(DrlStatus drlStatus);
    
    void resetDrlStatus();

}
