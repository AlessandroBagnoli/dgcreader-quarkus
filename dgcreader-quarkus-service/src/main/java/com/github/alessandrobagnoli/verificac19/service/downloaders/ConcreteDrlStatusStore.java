package com.github.alessandrobagnoli.verificac19.service.downloaders;

import javax.enterprise.context.ApplicationScoped;

import lombok.Getter;
import lombok.Setter;

@ApplicationScoped
@Getter
@Setter
public class ConcreteDrlStatusStore implements DrlStatusStore {

    private DrlStatus drlStatus;

    public ConcreteDrlStatusStore() {
        initializeDrlStatus();
    }

    @Override
    public void resetDrlStatus() {
        initializeDrlStatus();
    }

    private void initializeDrlStatus() {
        drlStatus = DrlStatus.builder()
            .version(0L)
            .chunk(0L)
            .build();
    }

}
