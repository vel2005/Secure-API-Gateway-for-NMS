package com.example.secure_api_gateway.dto;

import jakarta.validation.constraints.NotBlank;

public class AlarmStatusUpdateRequest {

    @NotBlank(message = "Status must not be blank")
    private String status;

    public AlarmStatusUpdateRequest() {
    }

    public AlarmStatusUpdateRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

