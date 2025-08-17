package com.swifteats.simulator.controller;

import com.swifteats.simulator.model.SimulatedDriver;
import com.swifteats.simulator.service.SimulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/simulator")
@RequiredArgsConstructor
@Slf4j
public class SimulatorController {

    private final SimulationService simulationService;

    @Value("${simulator.driver.count:10000}")
    private int driverCount;

    @Value("${simulator.update.interval:500}")
    private long updateIntervalMs;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSimulationStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("active", true);
        status.put("driverCount", driverCount);
        status.put("updateIntervalMs", updateIntervalMs);
        status.put("updatesPerSecond", 1000.0 / updateIntervalMs * driverCount);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/control/restart")
    public ResponseEntity<Map<String, String>> restartSimulation() {
        simulationService.initializeDrivers();
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Simulation restarted with " + driverCount + " drivers"));
    }

    @PostMapping("/control/driver-count")
    public ResponseEntity<Map<String, String>> setDriverCount(@RequestParam int count) {
        if (count < 1 || count > 50000) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Driver count must be between 1 and 50000"));
        }

        // Update the driver count field
        this.driverCount = count;

        // Restart the simulation with the new driver count
        simulationService.initializeDrivers();

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Driver count updated to " + count));
    }
}
