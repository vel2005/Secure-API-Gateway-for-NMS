package com.example.secure_api_gateway.controller;

import com.example.secure_api_gateway.dto.DeviceRequest;
import com.example.secure_api_gateway.entity.Device;
import com.example.secure_api_gateway.service.DeviceService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    public ResponseEntity<Device> createDevice(@Valid @RequestBody DeviceRequest request) {
        Device device = new Device(
                request.getDeviceName(),
                request.getDeviceType(),
                request.getIpAddress(),
                request.getStatus().toUpperCase()
        );
        Device created = deviceService.createDevice(device);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices() {
        List<Device> devices = deviceService.getAllDevices();
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable Long id) {
        Device device = deviceService.getDeviceById(id);
        return ResponseEntity.ok(device);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Device> updateDevice(@PathVariable Long id, @Valid @RequestBody DeviceRequest request) {
        Device deviceDetails = new Device(
                request.getDeviceName(),
                request.getDeviceType(),
                request.getIpAddress(),
                request.getStatus().toUpperCase()
        );
        Device updated = deviceService.updateDevice(id, deviceDetails);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Device>> getDevicesByStatus(@PathVariable String status) {
        List<Device> devices = deviceService.getDevicesByStatus(status);
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Device>> getDevicesByType(@PathVariable String type) {
        List<Device> devices = deviceService.getDevicesByType(type);
        return ResponseEntity.ok(devices);
    }
}

