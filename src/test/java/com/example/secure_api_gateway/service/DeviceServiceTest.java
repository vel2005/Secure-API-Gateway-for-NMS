package com.example.secure_api_gateway.service;

import com.example.secure_api_gateway.entity.Device;
import com.example.secure_api_gateway.exception.ResourceNotFoundException;
import com.example.secure_api_gateway.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private com.example.secure_api_gateway.repository.AlarmRepository alarmRepository;

    @InjectMocks
    private DeviceService deviceService;

    private Device sampleDevice;

    @BeforeEach
    void setUp() {
        sampleDevice = new Device(1L, "Router-Core-01", "Router", "192.168.1.1", "ONLINE");
    }

    @Test
    @DisplayName("Create device - success")
    void testCreateDevice_Success() {
        when(deviceRepository.save(any(Device.class))).thenReturn(sampleDevice);

        Device deviceToSave = new Device("Router-Core-01", "Router", "192.168.1.1", "ONLINE");
        Device created = deviceService.createDevice(deviceToSave);

        assertNotNull(created);
        assertEquals(1L, created.getId());
        assertEquals("Router-Core-01", created.getDeviceName());
        verify(deviceRepository, times(1)).save(deviceToSave);
    }

    @Test
    @DisplayName("Create device - invalid status throws IllegalArgumentException")
    void testCreateDevice_InvalidStatus() {
        Device invalidDevice = new Device("Router-Core-01", "Router", "192.168.1.1", "PENDING");

        assertThrows(IllegalArgumentException.class, () -> deviceService.createDevice(invalidDevice));
        verify(deviceRepository, never()).save(any(Device.class));
    }

    @Test
    @DisplayName("Get all devices - returns list")
    void testGetAllDevices() {
        Device device2 = new Device(2L, "Switch-Dist-01", "Switch", "192.168.1.2", "OFFLINE");
        when(deviceRepository.findAll()).thenReturn(Arrays.asList(sampleDevice, device2));

        List<Device> devices = deviceService.getAllDevices();

        assertEquals(2, devices.size());
        verify(deviceRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Get device by ID - found")
    void testGetDeviceById_Found() {
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(sampleDevice));

        Device found = deviceService.getDeviceById(1L);

        assertNotNull(found);
        assertEquals("Router-Core-01", found.getDeviceName());
        verify(deviceRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Get device by ID - not found throws ResourceNotFoundException")
    void testGetDeviceById_NotFound() {
        when(deviceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> deviceService.getDeviceById(99L));
        verify(deviceRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Update device - success")
    void testUpdateDevice_Success() {
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(sampleDevice));
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Device updateDetails = new Device("Router-Core-01-Updated", "Core-Router", "192.168.1.10", "OFFLINE");
        Device updated = deviceService.updateDevice(1L, updateDetails);

        assertNotNull(updated);
        assertEquals("Router-Core-01-Updated", updated.getDeviceName());
        assertEquals("Core-Router", updated.getDeviceType());
        assertEquals("192.168.1.10", updated.getIpAddress());
        assertEquals("OFFLINE", updated.getStatus());
        verify(deviceRepository, times(1)).save(sampleDevice);
    }

    @Test
    @DisplayName("Update device - not found throws ResourceNotFoundException")
    void testUpdateDevice_NotFound() {
        when(deviceRepository.findById(99L)).thenReturn(Optional.empty());

        Device updateDetails = new Device("Router-Core-01-Updated", "Core-Router", "192.168.1.10", "OFFLINE");
        assertThrows(ResourceNotFoundException.class, () -> deviceService.updateDevice(99L, updateDetails));
        verify(deviceRepository, never()).save(any(Device.class));
    }

    @Test
    @DisplayName("Delete device - success")
    void testDeleteDevice_Success() {
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(sampleDevice));

        deviceService.deleteDevice(1L);

        verify(alarmRepository, times(1)).deleteByDeviceId(1L);
        verify(deviceRepository, times(1)).delete(sampleDevice);
    }

    @Test
    @DisplayName("Delete device - not found throws ResourceNotFoundException")
    void testDeleteDevice_NotFound() {
        when(deviceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> deviceService.deleteDevice(99L));
        verify(deviceRepository, never()).delete(any(Device.class));
    }

    @Test
    @DisplayName("Get devices by status - valid status")
    void testGetDevicesByStatus_Valid() {
        when(deviceRepository.findByStatusIgnoreCase("ONLINE")).thenReturn(List.of(sampleDevice));

        List<Device> devices = deviceService.getDevicesByStatus("ONLINE");

        assertEquals(1, devices.size());
        verify(deviceRepository, times(1)).findByStatusIgnoreCase("ONLINE");
    }

    @Test
    @DisplayName("Get devices by status - invalid status throws IllegalArgumentException")
    void testGetDevicesByStatus_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> deviceService.getDevicesByStatus("UNKNOWN"));
        verify(deviceRepository, never()).findByStatusIgnoreCase(any());
    }

    @Test
    @DisplayName("Get devices by type - returns list")
    void testGetDevicesByType() {
        when(deviceRepository.findByDeviceTypeIgnoreCase("Router")).thenReturn(List.of(sampleDevice));

        List<Device> devices = deviceService.getDevicesByType("Router");

        assertEquals(1, devices.size());
        verify(deviceRepository, times(1)).findByDeviceTypeIgnoreCase("Router");
    }
}
