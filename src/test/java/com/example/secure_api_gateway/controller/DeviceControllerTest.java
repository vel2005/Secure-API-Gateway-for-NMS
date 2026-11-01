package com.example.secure_api_gateway.controller;

import com.example.secure_api_gateway.entity.Device;
import com.example.secure_api_gateway.exception.GlobalExceptionHandler;
import com.example.secure_api_gateway.exception.ResourceNotFoundException;
import com.example.secure_api_gateway.service.DeviceService;
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
class DeviceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DeviceService deviceService;

    @InjectMocks
    private DeviceController deviceController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(deviceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/devices - 201 Created on valid input")
    void testCreateDevice_Success() throws Exception {
        Device created = new Device(1L, "Router-01", "Router", "192.168.1.1", "ONLINE");
        when(deviceService.createDevice(any(Device.class))).thenReturn(created);

        String json = "{\"deviceName\":\"Router-01\",\"deviceType\":\"Router\",\"ipAddress\":\"192.168.1.1\",\"status\":\"ONLINE\"}";

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.deviceName", is("Router-01")))
                .andExpect(jsonPath("$.ipAddress", is("192.168.1.1")))
                .andExpect(jsonPath("$.status", is("ONLINE")));
    }

    @Test
    @DisplayName("POST /api/devices - 400 Bad Request on invalid IPv4")
    void testCreateDevice_InvalidIp() throws Exception {
        String json = "{\"deviceName\":\"Router-01\",\"deviceType\":\"Router\",\"ipAddress\":\"999.999.999.999\",\"status\":\"ONLINE\"}";

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.validationErrors.ipAddress").exists());
    }

    @Test
    @DisplayName("POST /api/devices - 400 Bad Request on blank device name")
    void testCreateDevice_BlankDeviceName() throws Exception {
        String json = "{\"deviceName\":\"\",\"deviceType\":\"Router\",\"ipAddress\":\"192.168.1.1\",\"status\":\"ONLINE\"}";

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.validationErrors.deviceName").exists());
    }

    @Test
    @DisplayName("POST /api/devices - 400 Bad Request on invalid status")
    void testCreateDevice_InvalidStatus() throws Exception {
        String json = "{\"deviceName\":\"Router-01\",\"deviceType\":\"Router\",\"ipAddress\":\"192.168.1.1\",\"status\":\"UNKNOWN\"}";

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.validationErrors.status").exists());
    }

    @Test
    @DisplayName("GET /api/devices - 200 OK returns all devices")
    void testGetAllDevices() throws Exception {
        Device d1 = new Device(1L, "Router-01", "Router", "192.168.1.1", "ONLINE");
        Device d2 = new Device(2L, "Switch-01", "Switch", "192.168.1.2", "OFFLINE");

        when(deviceService.getAllDevices()).thenReturn(Arrays.asList(d1, d2));

        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].deviceName", is("Router-01")))
                .andExpect(jsonPath("$[1].deviceName", is("Switch-01")));
    }

    @Test
    @DisplayName("GET /api/devices/{id} - 200 OK on existing ID")
    void testGetDeviceById_Success() throws Exception {
        Device d1 = new Device(1L, "Router-01", "Router", "192.168.1.1", "ONLINE");
        when(deviceService.getDeviceById(1L)).thenReturn(d1);

        mockMvc.perform(get("/api/devices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.deviceName", is("Router-01")));
    }

    @Test
    @DisplayName("GET /api/devices/{id} - 404 Not Found on missing ID")
    void testGetDeviceById_NotFound() throws Exception {
        when(deviceService.getDeviceById(99L))
                .thenThrow(new ResourceNotFoundException("Device not found with id: 99"));

        mockMvc.perform(get("/api/devices/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Device not found with id: 99")));
    }

    @Test
    @DisplayName("PUT /api/devices/{id} - 200 OK on valid update")
    void testUpdateDevice_Success() throws Exception {
        Device updated = new Device(1L, "Router-01-Updated", "Core-Router", "192.168.1.10", "OFFLINE");
        when(deviceService.updateDevice(eq(1L), any(Device.class))).thenReturn(updated);

        String json = "{\"deviceName\":\"Router-01-Updated\",\"deviceType\":\"Core-Router\",\"ipAddress\":\"192.168.1.10\",\"status\":\"OFFLINE\"}";

        mockMvc.perform(put("/api/devices/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.deviceName", is("Router-01-Updated")))
                .andExpect(jsonPath("$.status", is("OFFLINE")));
    }

    @Test
    @DisplayName("PUT /api/devices/{id} - 404 Not Found on missing ID")
    void testUpdateDevice_NotFound() throws Exception {
        when(deviceService.updateDevice(eq(99L), any(Device.class)))
                .thenThrow(new ResourceNotFoundException("Device not found with id: 99"));

        String json = "{\"deviceName\":\"Router-01-Updated\",\"deviceType\":\"Core-Router\",\"ipAddress\":\"192.168.1.10\",\"status\":\"OFFLINE\"}";

        mockMvc.perform(put("/api/devices/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("DELETE /api/devices/{id} - 204 No Content on success")
    void testDeleteDevice_Success() throws Exception {
        doNothing().when(deviceService).deleteDevice(1L);

        mockMvc.perform(delete("/api/devices/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/devices/{id} - 404 Not Found on missing ID")
    void testDeleteDevice_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Device not found with id: 99"))
                .when(deviceService).deleteDevice(99L);

        mockMvc.perform(delete("/api/devices/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("GET /api/devices/status/{status} - 200 OK")
    void testGetDevicesByStatus_Success() throws Exception {
        Device d1 = new Device(1L, "Router-01", "Router", "192.168.1.1", "ONLINE");
        when(deviceService.getDevicesByStatus("ONLINE")).thenReturn(List.of(d1));

        mockMvc.perform(get("/api/devices/status/ONLINE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("ONLINE")));
    }

    @Test
    @DisplayName("GET /api/devices/status/{status} - 400 Bad Request on invalid status")
    void testGetDevicesByStatus_InvalidStatus() throws Exception {
        when(deviceService.getDevicesByStatus("INVALID"))
                .thenThrow(new IllegalArgumentException("Invalid device status: 'INVALID'. Status must be either ONLINE or OFFLINE"));

        mockMvc.perform(get("/api/devices/status/INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Invalid device status: 'INVALID'. Status must be either ONLINE or OFFLINE")));
    }

    @Test
    @DisplayName("GET /api/devices/type/{type} - 200 OK")
    void testGetDevicesByType_Success() throws Exception {
        Device d1 = new Device(1L, "Router-01", "Router", "192.168.1.1", "ONLINE");
        when(deviceService.getDevicesByType("Router")).thenReturn(List.of(d1));

        mockMvc.perform(get("/api/devices/type/Router"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].deviceType", is("Router")));
    }
}
