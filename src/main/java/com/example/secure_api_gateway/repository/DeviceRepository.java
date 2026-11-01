package com.example.secure_api_gateway.repository;

import com.example.secure_api_gateway.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    List<Device> findByStatus(String status);

    List<Device> findByStatusIgnoreCase(String status);

    List<Device> findByDeviceType(String deviceType);

    List<Device> findByDeviceTypeIgnoreCase(String deviceType);
}

