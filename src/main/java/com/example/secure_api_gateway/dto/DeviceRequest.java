package com.example.secure_api_gateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class DeviceRequest {

    @NotBlank(message = "Device name must not be blank")
    private String deviceName;

    @NotBlank(message = "Device type must not be blank")
    private String deviceType;

    @NotBlank(message = "IP address must not be blank")
    @Pattern(
            regexp = "^((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])$",
            message = "IP address must be a valid IPv4 address"
    )
    private String ipAddress;

    @NotBlank(message = "Status must not be blank")
    @Pattern(
            regexp = "^(ONLINE|OFFLINE)$",
            message = "Status must be either ONLINE or OFFLINE"
    )
    private String status;

    public DeviceRequest() {
    }

    public DeviceRequest(String deviceName, String deviceType, String ipAddress, String status) {
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.ipAddress = ipAddress;
        this.status = status;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

