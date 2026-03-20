package com.licensing.service;

import com.licensing.entities.Device;
import com.licensing.entities.User;
import com.licensing.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;

    public Device findDeviceByMac(String macAddress) throws Exception {
        return deviceRepository.findByMacAddress(macAddress)
                .orElseThrow(() -> new Exception("Device not found with MAC: " + macAddress));
    }

    @Transactional
    public Device getOrCreateDevice(String macAddress, String deviceName, User user) {
        return deviceRepository.findByMacAddress(macAddress)
                .orElseGet(() -> {
                    Device newDevice = new Device();
                    newDevice.setMacAddress(macAddress);
                    newDevice.setName(deviceName != null ? deviceName : "Unknown Device");
                    newDevice.setUser(user);
                    return deviceRepository.save(newDevice);
                });
    }
}