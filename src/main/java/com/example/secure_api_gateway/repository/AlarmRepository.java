package com.example.secure_api_gateway.repository;

import com.example.secure_api_gateway.entity.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    List<Alarm> findByDeviceId(Long deviceId);

    List<Alarm> findBySeverity(String severity);

    List<Alarm> findBySeverityIgnoreCase(String severity);

    List<Alarm> findByStatus(String status);

    List<Alarm> findByStatusIgnoreCase(String status);

    void deleteByDeviceId(Long deviceId);
}

