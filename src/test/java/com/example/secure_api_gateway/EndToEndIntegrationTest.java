package com.example.secure_api_gateway;

import com.example.secure_api_gateway.dto.AlarmRequest;
import com.example.secure_api_gateway.dto.AlarmStatusUpdateRequest;
import com.example.secure_api_gateway.dto.DeviceRequest;
import com.example.secure_api_gateway.entity.Alarm;
import com.example.secure_api_gateway.entity.Device;
import com.example.secure_api_gateway.repository.AlarmRepository;
import com.example.secure_api_gateway.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("Phase 2 test suite - disabled until Phase 5 JWT Resource Server integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EndToEndIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private AlarmRepository alarmRepository;

    private RestClient restClient;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeEach
    void setUp() {
        alarmRepository.deleteAll();
        deviceRepository.deleteAll();
        restClient = RestClient.builder().baseUrl(getBaseUrl()).build();
    }

    @Test
    @DisplayName("E2E Device & Alarm Lifecycle Test")
    void testFullDeviceAndAlarmLifecycle() {
        // 1. Create a Device -> 201 Created
        DeviceRequest devReq = new DeviceRequest("Edge-Router-01", "Router", "10.10.1.1", "ONLINE");
        ResponseEntity<Device> createDevResp = restClient.post()
                .uri("/api/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .body(devReq)
                .retrieve()
                .toEntity(Device.class);

        assertEquals(HttpStatus.CREATED, createDevResp.getStatusCode());
        Device createdDevice = createDevResp.getBody();
        assertNotNull(createdDevice);
        assertNotNull(createdDevice.getId());
        assertEquals("Edge-Router-01", createdDevice.getDeviceName());
        assertEquals("10.10.1.1", createdDevice.getIpAddress());
        assertEquals("ONLINE", createdDevice.getStatus());

        Long deviceId = createdDevice.getId();

        // 2. Get Device by ID -> 200 OK
        ResponseEntity<Device> getDevResp = restClient.get()
                .uri("/api/devices/{id}", deviceId)
                .retrieve()
                .toEntity(Device.class);

        assertEquals(HttpStatus.OK, getDevResp.getStatusCode());
        assertEquals("Edge-Router-01", getDevResp.getBody().getDeviceName());

        // 3. Get All Devices -> 200 OK
        ResponseEntity<Device[]> getAllDevResp = restClient.get()
                .uri("/api/devices")
                .retrieve()
                .toEntity(Device[].class);

        assertEquals(HttpStatus.OK, getAllDevResp.getStatusCode());
        assertTrue(getAllDevResp.getBody().length >= 1);

        // 4. Update Device -> 200 OK
        DeviceRequest updateDevReq = new DeviceRequest("Edge-Router-01-Renamed", "Core-Router", "10.10.1.2", "OFFLINE");
        ResponseEntity<Device> updateDevResp = restClient.put()
                .uri("/api/devices/{id}", deviceId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateDevReq)
                .retrieve()
                .toEntity(Device.class);

        assertEquals(HttpStatus.OK, updateDevResp.getStatusCode());
        assertEquals("Edge-Router-01-Renamed", updateDevResp.getBody().getDeviceName());
        assertEquals("OFFLINE", updateDevResp.getBody().getStatus());

        // 5. Filter Devices by Status -> 200 OK
        ResponseEntity<Device[]> getByStatusResp = restClient.get()
                .uri("/api/devices/status/OFFLINE")
                .retrieve()
                .toEntity(Device[].class);

        assertEquals(HttpStatus.OK, getByStatusResp.getStatusCode());
        assertTrue(getByStatusResp.getBody().length >= 1);

        // 6. Filter Devices by Type -> 200 OK
        ResponseEntity<Device[]> getByTypeResp = restClient.get()
                .uri("/api/devices/type/Core-Router")
                .retrieve()
                .toEntity(Device[].class);

        assertEquals(HttpStatus.OK, getByTypeResp.getStatusCode());
        assertTrue(getByTypeResp.getBody().length >= 1);

        // 7. Create Alarm for the Device -> 201 Created
        AlarmRequest alarmReq = new AlarmRequest(deviceId, "BGP_DOWN", "BGP session down with peer", "CRITICAL", "ACTIVE");
        ResponseEntity<Alarm> createAlarmResp = restClient.post()
                .uri("/api/alarms")
                .contentType(MediaType.APPLICATION_JSON)
                .body(alarmReq)
                .retrieve()
                .toEntity(Alarm.class);

        assertEquals(HttpStatus.CREATED, createAlarmResp.getStatusCode());
        Alarm createdAlarm = createAlarmResp.getBody();
        assertNotNull(createdAlarm);
        assertNotNull(createdAlarm.getId());
        assertEquals("BGP_DOWN", createdAlarm.getAlarmType());
        assertEquals("CRITICAL", createdAlarm.getSeverity());
        assertEquals(deviceId, createdAlarm.getDeviceId());

        Long alarmId = createdAlarm.getId();

        // 8. Get Alarm by ID -> 200 OK
        ResponseEntity<Alarm> getAlarmResp = restClient.get()
                .uri("/api/alarms/{id}", alarmId)
                .retrieve()
                .toEntity(Alarm.class);

        assertEquals(HttpStatus.OK, getAlarmResp.getStatusCode());
        assertEquals("BGP_DOWN", getAlarmResp.getBody().getAlarmType());

        // 9. Get Alarms by Device ID -> 200 OK
        ResponseEntity<Alarm[]> getAlarmsByDevResp = restClient.get()
                .uri("/api/alarms/device/{deviceId}", deviceId)
                .retrieve()
                .toEntity(Alarm[].class);

        assertEquals(HttpStatus.OK, getAlarmsByDevResp.getStatusCode());
        assertEquals(1, getAlarmsByDevResp.getBody().length);

        // 10. Filter Alarms by Severity -> 200 OK
        ResponseEntity<Alarm[]> getAlarmsBySevResp = restClient.get()
                .uri("/api/alarms/severity/CRITICAL")
                .retrieve()
                .toEntity(Alarm[].class);

        assertEquals(HttpStatus.OK, getAlarmsBySevResp.getStatusCode());
        assertEquals(1, getAlarmsBySevResp.getBody().length);

        // 11. Update Alarm Status -> 200 OK
        AlarmStatusUpdateRequest statusReq = new AlarmStatusUpdateRequest("CLEARED");
        ResponseEntity<Alarm> updateStatusResp = restClient.put()
                .uri("/api/alarms/{id}/status", alarmId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(statusReq)
                .retrieve()
                .toEntity(Alarm.class);

        assertEquals(HttpStatus.OK, updateStatusResp.getStatusCode());
        assertEquals("CLEARED", updateStatusResp.getBody().getStatus());

        // 12. Delete Alarm -> 204 No Content
        ResponseEntity<Void> deleteAlarmResp = restClient.delete()
                .uri("/api/alarms/{id}", alarmId)
                .retrieve()
                .toBodilessEntity();

        assertEquals(HttpStatus.NO_CONTENT, deleteAlarmResp.getStatusCode());

        // 13. Delete Device -> 204 No Content
        ResponseEntity<Void> deleteDevResp = restClient.delete()
                .uri("/api/devices/{id}", deviceId)
                .retrieve()
                .toBodilessEntity();

        assertEquals(HttpStatus.NO_CONTENT, deleteDevResp.getStatusCode());
    }

    @Test
    @DisplayName("E2E Validation and Error Handling Test")
    void testValidationAndErrors() {
        // 1. Invalid IP -> 400 Bad Request
        DeviceRequest invalidIpReq = new DeviceRequest("Bad-Dev", "Router", "300.1.1.1", "ONLINE");
        ResponseEntity<String> badIpResp = restClient.post()
                .uri("/api/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .body(invalidIpReq)
                .retrieve()
                .onStatus(status -> status.value() == 400, (req, resp) -> {})
                .toEntity(String.class);

        assertEquals(HttpStatus.BAD_REQUEST, badIpResp.getStatusCode());

        // 2. Invalid Status -> 400 Bad Request
        DeviceRequest invalidStatusReq = new DeviceRequest("Bad-Dev", "Router", "192.168.1.1", "PENDING");
        ResponseEntity<String> badStatusResp = restClient.post()
                .uri("/api/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .body(invalidStatusReq)
                .retrieve()
                .onStatus(status -> status.value() == 400, (req, resp) -> {})
                .toEntity(String.class);

        assertEquals(HttpStatus.BAD_REQUEST, badStatusResp.getStatusCode());

        // 3. Blank Name -> 400 Bad Request
        DeviceRequest blankNameReq = new DeviceRequest("", "Router", "192.168.1.1", "ONLINE");
        ResponseEntity<String> blankNameResp = restClient.post()
                .uri("/api/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .body(blankNameReq)
                .retrieve()
                .onStatus(status -> status.value() == 400, (req, resp) -> {})
                .toEntity(String.class);

        assertEquals(HttpStatus.BAD_REQUEST, blankNameResp.getStatusCode());

        // 4. Missing Device ID -> 404 Not Found
        ResponseEntity<String> missingDevResp = restClient.get()
                .uri("/api/devices/99999")
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, resp) -> {})
                .toEntity(String.class);

        assertEquals(HttpStatus.NOT_FOUND, missingDevResp.getStatusCode());

        // 5. Create Alarm with Non-existent Device ID -> 404 Not Found
        AlarmRequest alarmWithBadDev = new AlarmRequest(99999L, "LINK_DOWN", "Desc", "CRITICAL", "ACTIVE");
        ResponseEntity<String> alarmBadDevResp = restClient.post()
                .uri("/api/alarms")
                .contentType(MediaType.APPLICATION_JSON)
                .body(alarmWithBadDev)
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, resp) -> {})
                .toEntity(String.class);

        assertEquals(HttpStatus.NOT_FOUND, alarmBadDevResp.getStatusCode());

        // 6. Create Alarm with Invalid Severity -> 400 Bad Request
        AlarmRequest alarmWithBadSev = new AlarmRequest(1L, "LINK_DOWN", "Desc", "EXTREME", "ACTIVE");
        ResponseEntity<String> alarmBadSevResp = restClient.post()
                .uri("/api/alarms")
                .contentType(MediaType.APPLICATION_JSON)
                .body(alarmWithBadSev)
                .retrieve()
                .onStatus(status -> status.value() == 400, (req, resp) -> {})
                .toEntity(String.class);

        assertEquals(HttpStatus.BAD_REQUEST, alarmBadSevResp.getStatusCode());

        // 7. Filter Devices with Invalid Status -> 400 Bad Request
        ResponseEntity<String> invalidFilterStatusResp = restClient.get()
                .uri("/api/devices/status/INVALID_STATUS")
                .retrieve()
                .onStatus(status -> status.value() == 400, (req, resp) -> {})
                .toEntity(String.class);

        assertEquals(HttpStatus.BAD_REQUEST, invalidFilterStatusResp.getStatusCode());

        // 8. Filter Alarms with Invalid Severity -> 400 Bad Request
        ResponseEntity<String> invalidFilterSevResp = restClient.get()
                .uri("/api/alarms/severity/UNKNOWN_SEVERITY")
                .retrieve()
                .onStatus(status -> status.value() == 400, (req, resp) -> {})
                .toEntity(String.class);

        assertEquals(HttpStatus.BAD_REQUEST, invalidFilterSevResp.getStatusCode());
    }
}

