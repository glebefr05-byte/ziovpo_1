package com.licensing.controller;

import com.licensing.entities.User;
import com.licensing.entities.Device;
import com.licensing.model.DeviceDto;
import com.licensing.repository.UserRepository;
import com.licensing.repository.DeviceRepository;
import com.licensing.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final UserService currentUserService;

    private DeviceDto convertToDto(Device device) {
        DeviceDto dto = new DeviceDto();
        dto.setId(device.getId());
        dto.setName(device.getName());
        dto.setMacAddress(device.getMacAddress());
        dto.setUserId(device.getUser().getId());
        return dto;
    }

    @GetMapping
    public ResponseEntity<List<DeviceDto>> getAllDevices() {
        List<DeviceDto> devices = deviceRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceDto> getDeviceById(@PathVariable UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Device not found with id: " + id));
        return ResponseEntity.ok(convertToDto(device));
    }

    @GetMapping("/mac/{macAddress}")
    public ResponseEntity<DeviceDto> getDeviceByMac(@PathVariable String macAddress) {
        Device device = deviceRepository.findFirstByMacAddress(macAddress)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Device not found with MAC: " + macAddress));
        return ResponseEntity.ok(convertToDto(device));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DeviceDto>> getDevicesByUser(@PathVariable UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User not found with id: " + userId);
        }

        List<DeviceDto> devices = deviceRepository.findAll().stream()
                .filter(device -> device.getUser().getId().equals(userId))
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/my-devices")
    public ResponseEntity<List<DeviceDto>> getMyDevices() {
        UUID currentUserId = currentUserService.getUserId();

        List<DeviceDto> devices = deviceRepository.findAll().stream()
                .filter(device -> device.getUser().getId().equals(currentUserId))
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(devices);
    }

    @PostMapping
    public ResponseEntity<DeviceDto> createDevice(@Valid @RequestBody DeviceDto deviceDto) {
        if (deviceRepository.existsByMacAddress(deviceDto.getMacAddress())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Device with MAC address " + deviceDto.getMacAddress() + " already exists");
        }

        User user = userRepository.findById(deviceDto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with id: " + deviceDto.getUserId()));

        Device device = new Device();
        device.setName(deviceDto.getName());
        device.setMacAddress(deviceDto.getMacAddress());
        device.setUser(user);

        Device savedDevice = deviceRepository.save(device);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(savedDevice));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceDto> updateDevice(@PathVariable UUID id,
                                                  @Valid @RequestBody DeviceDto deviceDto) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Device not found with id: " + id));

        if (!device.getMacAddress().equals(deviceDto.getMacAddress()) &&
                deviceRepository.existsByMacAddress(deviceDto.getMacAddress())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Device with MAC address " + deviceDto.getMacAddress() + " already exists");
        }

        device.setName(deviceDto.getName());
        device.setMacAddress(deviceDto.getMacAddress());

        if (deviceDto.getUserId() != null && !device.getUser().getId().equals(deviceDto.getUserId())) {
            User newUser = userRepository.findById(deviceDto.getUserId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "User not found with id: " + deviceDto.getUserId()));
            device.setUser(newUser);
        }

        Device updatedDevice = deviceRepository.save(device);
        return ResponseEntity.ok(convertToDto(updatedDevice));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDevice(@PathVariable UUID id) {
        if (!deviceRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Device not found with id: " + id);
        }

        deviceRepository.deleteById(id);
        return ResponseEntity.ok(Map.of(
                "message", "Device deleted successfully",
                "id", id.toString()
        ));
    }

    @PatchMapping("/{id}/rename")
    public ResponseEntity<DeviceDto> renameDevice(@PathVariable UUID id,
                                                  @RequestBody Map<String, String> request) {
        String newName = request.get("name");
        if (newName == null || newName.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Name is required");
        }

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Device not found with id: " + id));

        device.setName(newName);
        Device updatedDevice = deviceRepository.save(device);
        return ResponseEntity.ok(convertToDto(updatedDevice));
    }
}