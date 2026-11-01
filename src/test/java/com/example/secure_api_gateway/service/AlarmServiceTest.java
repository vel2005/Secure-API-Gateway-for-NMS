package com.example.secure_api_gateway.service;

import com.example.secure_api_gateway.entity.Alarm;
import com.example.secure_api_gateway.exception.ResourceNotFoundException;
import com.example.secure_api_gateway.repository.AlarmRepository;
import com.example.secure_api_gateway.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlarmServiceTest {

    @Mock
    private AlarmRepository alarmRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private AlarmService alarmService;

    private Alarm sampleAlarm;

    @BeforeEach
    void setUp() {
        sampleAlarm = new Alarm(1L, 1L, "LINK_DOWN", "Port 1 link down", "CRITICAL", "ACTIVE", LocalDateTime.now());
    }

    @Test
    @DisplayName("Create alarm - success when device exists")
    void testCreateAlarm_Success() {
        when(deviceRepository.existsById(1L)).thenReturn(true);
        when(alarmRepository.save(any(Alarm.class))).thenReturn(sampleAlarm);

        Alarm toSave = new Alarm(1L, "LINK_DOWN", "Port 1 link down", "CRITICAL", "ACTIVE");
        Alarm created = alarmService.createAlarm(toSave);

        assertNotNull(created);
        assertEquals(1L, created.getId());
        assertEquals("LINK_DOWN", created.getAlarmType());
        verify(deviceRepository, times(1)).existsById(1L);
        verify(alarmRepository, times(1)).save(toSave);
    }

    @Test
    @DisplayName("Create alarm - device not found throws ResourceNotFoundException")
    void testCreateAlarm_DeviceNotFound() {
        when(deviceRepository.existsById(99L)).thenReturn(false);

        Alarm toSave = new Alarm(99L, "LINK_DOWN", "Port 1 link down", "CRITICAL", "ACTIVE");
        assertThrows(ResourceNotFoundException.class, () -> alarmService.createAlarm(toSave));
        verify(alarmRepository, never()).save(any(Alarm.class));
    }

    @Test
    @DisplayName("Create alarm - invalid severity throws IllegalArgumentException")
    void testCreateAlarm_InvalidSeverity() {
        when(deviceRepository.existsById(1L)).thenReturn(true);

        Alarm toSave = new Alarm(1L, "LINK_DOWN", "Port 1 link down", "EXTREME", "ACTIVE");
        assertThrows(IllegalArgumentException.class, () -> alarmService.createAlarm(toSave));
        verify(alarmRepository, never()).save(any(Alarm.class));
    }

    @Test
    @DisplayName("Get all alarms - returns list")
    void testGetAllAlarms() {
        Alarm alarm2 = new Alarm(2L, 1L, "HIGH_CPU", "CPU utilization > 90%", "MAJOR", "ACTIVE", LocalDateTime.now());
        when(alarmRepository.findAll()).thenReturn(Arrays.asList(sampleAlarm, alarm2));

        List<Alarm> alarms = alarmService.getAllAlarms();

        assertEquals(2, alarms.size());
        verify(alarmRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Get alarm by ID - found")
    void testGetAlarmById_Found() {
        when(alarmRepository.findById(1L)).thenReturn(Optional.of(sampleAlarm));

        Alarm found = alarmService.getAlarmById(1L);

        assertNotNull(found);
        assertEquals("LINK_DOWN", found.getAlarmType());
        verify(alarmRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Get alarm by ID - not found throws ResourceNotFoundException")
    void testGetAlarmById_NotFound() {
        when(alarmRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> alarmService.getAlarmById(99L));
        verify(alarmRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Get alarms by device ID - success when device exists")
    void testGetAlarmsByDeviceId_Success() {
        when(deviceRepository.existsById(1L)).thenReturn(true);
        when(alarmRepository.findByDeviceId(1L)).thenReturn(List.of(sampleAlarm));

        List<Alarm> alarms = alarmService.getAlarmsByDeviceId(1L);

        assertEquals(1, alarms.size());
        verify(deviceRepository, times(1)).existsById(1L);
        verify(alarmRepository, times(1)).findByDeviceId(1L);
    }

    @Test
    @DisplayName("Get alarms by device ID - device not found throws ResourceNotFoundException")
    void testGetAlarmsByDeviceId_NotFound() {
        when(deviceRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> alarmService.getAlarmsByDeviceId(99L));
        verify(alarmRepository, never()).findByDeviceId(any());
    }

    @Test
    @DisplayName("Get alarms by severity - valid severity")
    void testGetAlarmsBySeverity_Valid() {
        when(alarmRepository.findBySeverityIgnoreCase("CRITICAL")).thenReturn(List.of(sampleAlarm));

        List<Alarm> alarms = alarmService.getAlarmsBySeverity("CRITICAL");

        assertEquals(1, alarms.size());
        verify(alarmRepository, times(1)).findBySeverityIgnoreCase("CRITICAL");
    }

    @Test
    @DisplayName("Get alarms by severity - invalid severity throws IllegalArgumentException")
    void testGetAlarmsBySeverity_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> alarmService.getAlarmsBySeverity("URGENT"));
        verify(alarmRepository, never()).findBySeverityIgnoreCase(any());
    }

    @Test
    @DisplayName("Update alarm status - success")
    void testUpdateAlarmStatus_Success() {
        when(alarmRepository.findById(1L)).thenReturn(Optional.of(sampleAlarm));
        when(alarmRepository.save(any(Alarm.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Alarm updated = alarmService.updateAlarmStatus(1L, "CLEARED");

        assertNotNull(updated);
        assertEquals("CLEARED", updated.getStatus());
        verify(alarmRepository, times(1)).save(sampleAlarm);
    }

    @Test
    @DisplayName("Update alarm status - not found throws ResourceNotFoundException")
    void testUpdateAlarmStatus_NotFound() {
        when(alarmRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> alarmService.updateAlarmStatus(99L, "CLEARED"));
        verify(alarmRepository, never()).save(any(Alarm.class));
    }

    @Test
    @DisplayName("Delete alarm - success")
    void testDeleteAlarm_Success() {
        when(alarmRepository.findById(1L)).thenReturn(Optional.of(sampleAlarm));

        alarmService.deleteAlarm(1L);

        verify(alarmRepository, times(1)).delete(sampleAlarm);
    }

    @Test
    @DisplayName("Delete alarm - not found throws ResourceNotFoundException")
    void testDeleteAlarm_NotFound() {
        when(alarmRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> alarmService.deleteAlarm(99L));
        verify(alarmRepository, never()).delete(any(Alarm.class));
    }
}

