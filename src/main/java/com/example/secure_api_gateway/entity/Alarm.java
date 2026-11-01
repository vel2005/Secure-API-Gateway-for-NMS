package com.example.secure_api_gateway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "alarms")
public class Alarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Device ID must not be null")
    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @NotBlank(message = "Alarm type must not be blank")
    @Column(name = "alarm_type", nullable = false)
    private String alarmType;

    @NotBlank(message = "Description must not be blank")
    @Column(name = "description", nullable = false)
    private String description;

    @NotBlank(message = "Severity must not be blank")
    @Pattern(
            regexp = "^(CRITICAL|MAJOR|MINOR|WARNING)$",
            message = "Severity must be one of: CRITICAL, MAJOR, MINOR, WARNING"
    )
    @Column(name = "severity", nullable = false)
    private String severity;

    @NotBlank(message = "Status must not be blank")
    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Alarm() {
    }

    public Alarm(Long deviceId, String alarmType, String description, String severity, String status) {
        this.deviceId = deviceId;
        this.alarmType = alarmType;
        this.description = description;
        this.severity = severity;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public Alarm(Long id, Long deviceId, String alarmType, String description, String severity, String status, LocalDateTime createdAt) {
        this.id = id;
        this.deviceId = deviceId;
        this.alarmType = alarmType;
        this.description = description;
        this.severity = severity;
        this.status = status;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Alarm alarm = (Alarm) o;
        return Objects.equals(id, alarm.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Alarm{" +
                "id=" + id +
                ", deviceId=" + deviceId +
                ", alarmType='" + alarmType + '\'' +
                ", description='" + description + '\'' +
                ", severity='" + severity + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

