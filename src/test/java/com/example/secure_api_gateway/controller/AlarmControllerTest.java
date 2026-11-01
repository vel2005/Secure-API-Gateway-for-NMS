package com.example.secure_api_gateway.controller;

import com.example.secure_api_gateway.entity.Alarm;
import com.example.secure_api_gateway.exception.GlobalExceptionHandler;
import com.example.secure_api_gateway.exception.ResourceNotFoundException;
import com.example.secure_api_gateway.service.AlarmService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AlarmControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AlarmService alarmService;

    @InjectMocks
    private AlarmController alarmController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(alarmController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/alarms - 201 Created on valid input")
    void testCreateAlarm_Success() throws Exception {
        Alarm created = new Alarm(1L, 1L, "LINK_DOWN", "Port 1 interface down", "CRITICAL", "ACTIVE", LocalDateTime.now());
        when(alarmService.createAlarm(any(Alarm.class))).thenReturn(created);

        String json = "{\"deviceId\":1,\"alarmType\":\"LINK_DOWN\",\"description\":\"Port 1 interface down\",\"severity\":\"CRITICAL\",\"status\":\"ACTIVE\"}";

        mockMvc.perform(post("/api/alarms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.alarmType", is("LINK_DOWN")))
                .andExpect(jsonPath("$.severity", is("CRITICAL")))
                .andExpect(jsonPath("$.deviceId", is(1)));
    }

    @Test
    @DisplayName("POST /api/alarms - 400 Bad Request on invalid severity")
    void testCreateAlarm_InvalidSeverity() throws Exception {
        String json = "{\"deviceId\":1,\"alarmType\":\"LINK_DOWN\",\"description\":\"Port 1 interface down\",\"severity\":\"INVALID_SEV\",\"status\":\"ACTIVE\"}";

        mockMvc.perform(post("/api/alarms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.validationErrors.severity").exists());
    }

    @Test
    @DisplayName("POST /api/alarms - 404 Not Found when referenced device does not exist")
    void testCreateAlarm_DeviceNotFound() throws Exception {
        when(alarmService.createAlarm(any(Alarm.class)))
                .thenThrow(new ResourceNotFoundException("Referenced device not found with id: 99"));

        String json = "{\"deviceId\":99,\"alarmType\":\"LINK_DOWN\",\"description\":\"Port 1 interface down\",\"severity\":\"CRITICAL\",\"status\":\"ACTIVE\"}";

        mockMvc.perform(post("/api/alarms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Referenced device not found with id: 99")));
    }

    @Test
    @DisplayName("GET /api/alarms - 200 OK returns all alarms")
    void testGetAllAlarms() throws Exception {
        Alarm a1 = new Alarm(1L, 1L, "LINK_DOWN", "Port 1 interface down", "CRITICAL", "ACTIVE", LocalDateTime.now());
        Alarm a2 = new Alarm(2L, 1L, "HIGH_TEMP", "Chassis temperature > 75C", "WARNING", "ACTIVE", LocalDateTime.now());

        when(alarmService.getAllAlarms()).thenReturn(Arrays.asList(a1, a2));

        mockMvc.perform(get("/api/alarms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].alarmType", is("LINK_DOWN")))
                .andExpect(jsonPath("$[1].alarmType", is("HIGH_TEMP")));
    }

    @Test
    @DisplayName("GET /api/alarms/{id} - 200 OK on existing ID")
    void testGetAlarmById_Success() throws Exception {
        Alarm a1 = new Alarm(1L, 1L, "LINK_DOWN", "Port 1 interface down", "CRITICAL", "ACTIVE", LocalDateTime.now());
        when(alarmService.getAlarmById(1L)).thenReturn(a1);

        mockMvc.perform(get("/api/alarms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.alarmType", is("LINK_DOWN")));
    }

    @Test
    @DisplayName("GET /api/alarms/{id} - 404 Not Found on missing ID")
    void testGetAlarmById_NotFound() throws Exception {
        when(alarmService.getAlarmById(99L))
                .thenThrow(new ResourceNotFoundException("Alarm not found with id: 99"));

        mockMvc.perform(get("/api/alarms/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Alarm not found with id: 99")));
    }

    @Test
    @DisplayName("GET /api/alarms/device/{deviceId} - 200 OK on existing device")
    void testGetAlarmsByDeviceId_Success() throws Exception {
        Alarm a1 = new Alarm(1L, 1L, "LINK_DOWN", "Port 1 interface down", "CRITICAL", "ACTIVE", LocalDateTime.now());
        when(alarmService.getAlarmsByDeviceId(1L)).thenReturn(List.of(a1));

        mockMvc.perform(get("/api/alarms/device/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].deviceId", is(1)));
    }

    @Test
    @DisplayName("GET /api/alarms/device/{deviceId} - 404 Not Found on missing device")
    void testGetAlarmsByDeviceId_DeviceNotFound() throws Exception {
        when(alarmService.getAlarmsByDeviceId(99L))
                .thenThrow(new ResourceNotFoundException("Device not found with id: 99"));

        mockMvc.perform(get("/api/alarms/device/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Device not found with id: 99")));
    }

    @Test
    @DisplayName("GET /api/alarms/severity/{severity} - 200 OK on valid severity")
    void testGetAlarmsBySeverity_Success() throws Exception {
        Alarm a1 = new Alarm(1L, 1L, "LINK_DOWN", "Port 1 interface down", "CRITICAL", "ACTIVE", LocalDateTime.now());
        when(alarmService.getAlarmsBySeverity("CRITICAL")).thenReturn(List.of(a1));

        mockMvc.perform(get("/api/alarms/severity/CRITICAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].severity", is("CRITICAL")));
    }

    @Test
    @DisplayName("GET /api/alarms/severity/{severity} - 400 Bad Request on invalid severity")
    void testGetAlarmsBySeverity_InvalidSeverity() throws Exception {
        when(alarmService.getAlarmsBySeverity("INVALID"))
                .thenThrow(new IllegalArgumentException("Invalid alarm severity: 'INVALID'. Severity must be one of: CRITICAL, MAJOR, MINOR, WARNING"));

        mockMvc.perform(get("/api/alarms/severity/INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Invalid alarm severity: 'INVALID'. Severity must be one of: CRITICAL, MAJOR, MINOR, WARNING")));
    }

    @Test
    @DisplayName("PUT /api/alarms/{id}/status - 200 OK via JSON body")
    void testUpdateAlarmStatus_Success() throws Exception {
        Alarm updated = new Alarm(1L, 1L, "LINK_DOWN", "Port 1 interface down", "CRITICAL", "CLEARED", LocalDateTime.now());
        when(alarmService.updateAlarmStatus(eq(1L), eq("CLEARED"))).thenReturn(updated);

        String json = "{\"status\":\"CLEARED\"}";

        mockMvc.perform(put("/api/alarms/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("CLEARED")));
    }

    @Test
    @DisplayName("PUT /api/alarms/{id}/status - 404 Not Found on missing alarm")
    void testUpdateAlarmStatus_NotFound() throws Exception {
        when(alarmService.updateAlarmStatus(eq(99L), eq("CLEARED")))
                .thenThrow(new ResourceNotFoundException("Alarm not found with id: 99"));

        String json = "{\"status\":\"CLEARED\"}";

        mockMvc.perform(put("/api/alarms/99/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("DELETE /api/alarms/{id} - 204 No Content on success")
    void testDeleteAlarm_Success() throws Exception {
        doNothing().when(alarmService).deleteAlarm(1L);

        mockMvc.perform(delete("/api/alarms/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/alarms/{id} - 404 Not Found on missing alarm")
    void testDeleteAlarm_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Alarm not found with id: 99"))
                .when(alarmService).deleteAlarm(99L);

        mockMvc.perform(delete("/api/alarms/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }
}
