package com.example.secure_api_gateway.controller;

import com.example.secure_api_gateway.dto.AlarmRequest;
import com.example.secure_api_gateway.dto.AlarmStatusUpdateRequest;
import com.example.secure_api_gateway.entity.Alarm;
import com.example.secure_api_gateway.service.AlarmService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alarms")
public class AlarmController {

    private final AlarmService alarmService;

    public AlarmController(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    @PostMapping
    public ResponseEntity<Alarm> createAlarm(@Valid @RequestBody AlarmRequest request) {
        Alarm alarm = new Alarm(
                request.getDeviceId(),
                request.getAlarmType(),
                request.getDescription(),
                request.getSeverity().toUpperCase(),
                request.getStatus()
        );
        Alarm created = alarmService.createAlarm(alarm);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Alarm>> getAllAlarms() {
        List<Alarm> alarms = alarmService.getAllAlarms();
        return ResponseEntity.ok(alarms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Alarm> getAlarmById(@PathVariable Long id) {
        Alarm alarm = alarmService.getAlarmById(id);
        return ResponseEntity.ok(alarm);
    }

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<Alarm>> getAlarmsByDeviceId(@PathVariable Long deviceId) {
        List<Alarm> alarms = alarmService.getAlarmsByDeviceId(deviceId);
        return ResponseEntity.ok(alarms);
    }

    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<Alarm>> getAlarmsBySeverity(@PathVariable String severity) {
        List<Alarm> alarms = alarmService.getAlarmsBySeverity(severity);
        return ResponseEntity.ok(alarms);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Alarm> updateAlarmStatus(
            @PathVariable Long id,
            @RequestBody(required = false) AlarmStatusUpdateRequest statusUpdateRequest,
            @RequestParam(required = false) String status) {

        String newStatus = null;
        if (statusUpdateRequest != null && statusUpdateRequest.getStatus() != null && !statusUpdateRequest.getStatus().trim().isEmpty()) {
            newStatus = statusUpdateRequest.getStatus().trim();
        } else if (status != null && !status.trim().isEmpty()) {
            newStatus = status.trim();
        }

        if (newStatus == null) {
            throw new IllegalArgumentException("Alarm status must not be blank");
        }

        Alarm updated = alarmService.updateAlarmStatus(id, newStatus);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlarm(@PathVariable Long id) {
        alarmService.deleteAlarm(id);
        return ResponseEntity.noContent().build();
    }
}

