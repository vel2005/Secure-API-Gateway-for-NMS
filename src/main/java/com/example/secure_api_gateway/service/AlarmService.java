package com.example.secure_api_gateway.service;

import com.example.secure_api_gateway.entity.Alarm;
import com.example.secure_api_gateway.exception.ResourceNotFoundException;
import com.example.secure_api_gateway.repository.AlarmRepository;
import com.example.secure_api_gateway.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final DeviceRepository deviceRepository;

    public AlarmService(AlarmRepository alarmRepository, DeviceRepository deviceRepository) {
        this.alarmRepository = alarmRepository;
        this.deviceRepository = deviceRepository;
    }

    public Alarm createAlarm(Alarm alarm) {
        alarm.setId(null);

        if (alarm.getDeviceId() == null) {
            throw new IllegalArgumentException("Device ID must not be null");
        }

        if (!deviceRepository.existsById(alarm.getDeviceId())) {
            throw new ResourceNotFoundException("Referenced device not found with id: " + alarm.getDeviceId());
        }

        validateSeverity(alarm.getSeverity());

        if (alarm.getCreatedAt() == null) {
            alarm.setCreatedAt(LocalDateTime.now());
        }

        return alarmRepository.save(alarm);
    }

    @Transactional(readOnly = true)
    public List<Alarm> getAllAlarms() {
        return alarmRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Alarm getAlarmById(Long id) {
        return alarmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alarm not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Alarm> getAlarmsByDeviceId(Long deviceId) {
        if (deviceId == null) {
            throw new IllegalArgumentException("Device ID must not be null");
        }
        if (!deviceRepository.existsById(deviceId)) {
            throw new ResourceNotFoundException("Device not found with id: " + deviceId);
        }
        return alarmRepository.findByDeviceId(deviceId);
    }

    @Transactional(readOnly = true)
    public List<Alarm> getAlarmsBySeverity(String severity) {
        validateSeverity(severity);
        return alarmRepository.findBySeverityIgnoreCase(severity);
    }

    public Alarm updateAlarmStatus(Long id, String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Alarm status must not be blank");
        }

        Alarm existingAlarm = getAlarmById(id);
        existingAlarm.setStatus(status.trim());
        return alarmRepository.save(existingAlarm);
    }

    public void deleteAlarm(Long id) {
        Alarm existingAlarm = getAlarmById(id);
        alarmRepository.delete(existingAlarm);
    }

    private void validateSeverity(String severity) {
        if (severity == null || (!severity.equalsIgnoreCase("CRITICAL")
                && !severity.equalsIgnoreCase("MAJOR")
                && !severity.equalsIgnoreCase("MINOR")
                && !severity.equalsIgnoreCase("WARNING"))) {
            throw new IllegalArgumentException("Invalid alarm severity: '" + severity + "'. Severity must be one of: CRITICAL, MAJOR, MINOR, WARNING");
        }
    }
}

