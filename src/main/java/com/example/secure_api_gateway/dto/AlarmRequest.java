package com.example.secure_api_gateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class AlarmRequest {

    @NotNull(message = "Device ID must not be null")
    private Long deviceId;

    @NotBlank(message = "Alarm type must not be blank")
    private String alarmType;

    @NotBlank(message = "Description must not be blank")
    private String description;

    @NotBlank(message = "Severity must not be blank")
    @Pattern(
            regexp = "^(CRITICAL|MAJOR|MINOR|WARNING)$",
            message = "Severity must be one of: CRITICAL, MAJOR, MINOR, WARNING"
    )
    private String severity;

    @NotBlank(message = "Status must not be blank")
    private String status;

    public AlarmRequest() {
    }

    public AlarmRequest(Long deviceId, String alarmType, String description, String severity, String status) {
        this.deviceId = deviceId;
        this.alarmType = alarmType;
        this.description = description;
        this.severity = severity;
        this.status = status;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

