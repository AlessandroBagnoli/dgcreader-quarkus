package com.github.alessandrobagnoli.verificac19.dto;

import java.time.LocalDateTime;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiErrorSchema {

    private LocalDateTime timestamp;
    private Status status;
    private List<String> errors;
    private String path;

}
