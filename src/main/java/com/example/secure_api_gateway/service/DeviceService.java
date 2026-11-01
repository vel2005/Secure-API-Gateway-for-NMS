
package com.example.secure_api_gateway.service;

import com.example.secure_api_gateway.entity.Device;
import com.example.secure_api_gateway.exception.ResourceNotFoundException;
import com.example.secure_api_gateway.repository.AlarmRepository;
import com.example.secure_api_gateway.repository.DeviceRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final AlarmRepository alarmRepository;

    public DeviceService(DeviceRepository deviceRepository,
                         AlarmRepository alarmRepository) {
        this.deviceRepository = deviceRepository;
        this.alarmRepository = alarmRepository;
    }

    public Device createDevice(Device device) {
        device.setId(null);
        validateDeviceStatus(device.getStatus());
        device.setStatus(device.getStatus().toUpperCase());
        return deviceRepository.save(device);
    }

    @Transactional(readOnly = true)
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Device getDeviceById(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Device not found with id: " + id));
    }

    public Device updateDevice(Long id, Device deviceDetails) {
        Device existingDevice = getDeviceById(id);

        validateDeviceStatus(deviceDetails.getStatus());

        existingDevice.setDeviceName(deviceDetails.getDeviceName());
        existingDevice.setDeviceType(deviceDetails.getDeviceType());
        existingDevice.setIpAddress(deviceDetails.getIpAddress());
        existingDevice.setStatus(deviceDetails.getStatus().toUpperCase());

        return deviceRepository.save(existingDevice);
    }

    public void deleteDevice(Long id) {
        Device existingDevice = getDeviceById(id);

        alarmRepository.deleteByDeviceId(id);
        deviceRepository.delete(existingDevice);
    }

    @Transactional(readOnly = true)
    public List<Device> getDevicesByStatus(String status) {
        validateDeviceStatus(status);
        return deviceRepository.findByStatusIgnoreCase(status);
    }

    @Transactional(readOnly = true)
    public List<Device> getDevicesByType(String deviceType) {
        if (deviceType == null || deviceType.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Device type must not be blank");
        }

        return deviceRepository.findByDeviceTypeIgnoreCase(deviceType);
    }

    @Transactional(readOnly = true)
    public List<Device> getDevicesByDeviceType(String deviceType) {
        return getDevicesByType(deviceType);
    }

    private void validateDeviceStatus(String status) {
        if (status == null ||
                (!status.equalsIgnoreCase("ONLINE") &&
                 !status.equalsIgnoreCase("OFFLINE"))) {

            throw new IllegalArgumentException(
                    "Invalid device status: '" + status +
                    "'. Status must be either ONLINE or OFFLINE");
        }
    }
}